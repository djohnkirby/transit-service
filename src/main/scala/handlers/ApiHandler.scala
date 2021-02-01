package handlers

import api.{Frame, Frames}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import gtfs.{HeadSign, Station}
import gtfs.models.StopTime
import gtfs.parser.GtfsFileReader
import org.apache.commons.io.FileUtils
import spray.json._

import java.io.File
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, Period}
import scala.jdk.CollectionConverters._

/**
  * Scala entrypoint for the API Gateway Lambda function from: https://github.com/swartzrock/aws-lambda-hello-scala
  */
object ApiHandler {

  /**
    * Handle a Lambda request indirectly via the API Gateway
    * @param request the Java HTTP request
    * @param context the Java Lambda context
    * @return the HTTP response
    */
  def handle(request: APIGatewayProxyRequestEvent, context: Context): Response = {
    //DanielTODO: implement
    val parameters = request.getQueryStringParameters
    val walkTime   = parameters.get("walkTime").toLong
    val station    = Station.withName(parameters.get("station"))

    val frame = getFrameMessage(station, walkTime)

    Response(Frames(Seq(Frame(frame))).toJson.toString, Map("Content-Type" -> "application/json"))
  }

  private def getFrameMessage(station: Station.Value, walkTime: Long): String = {
    val today = LocalDate.now()
    val now   = LocalDateTime.now()
    //I have a station. I know what time it is. Now I need to find out
    //what trains are leaving soon and where they're going.
    val stationIds = Station.stationIdMap.get(station).getOrElse(Set.empty)

    if (stationIds.isEmpty)
      return "Error, no stationIDs found for this station"

    val awsCredentials = new BasicAWSCredentials(
      "",
      ""
    )

    val amazonS3Client: AmazonS3 = AmazonS3ClientBuilder
      .standard()
      .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
      .withRegion(Regions.US_EAST_1)
      .build()

    FileUtils.copyInputStreamToFile(
      amazonS3Client.getObject("transitservice-data", "stop_times.txt").getObjectContent,
      new File("stop_times.txt")
    )

    FileUtils.copyInputStreamToFile(
      amazonS3Client.getObject("transitservice-data", "stops.txt").getObjectContent,
      new File("stops.txt")
    )

    FileUtils.copyInputStreamToFile(
      amazonS3Client.getObject("transitservice-data", "trips.txt").getObjectContent,
      new File("trips.txt")
    )

    val gtfsFileReader = new GtfsFileReader(".")

    var destinationsToTimesMap: Map[String, Set[Long]] = Map.empty

    val trips = gtfsFileReader.getTrips

    val stopTimes: Iterator[StopTime] = gtfsFileReader.getStopTimes
      .map(_.toStopTime(today, walkTime))
      .filter(stopTime => stationIds.contains(stopTime.stop_id))
      .filter(
        stopTime =>
          stopTime.departure
            .exists(departureTime => departureTime.isAfter(now) && departureTime.isBefore(now.plusHours(2)))
      )

    stopTimes.foreach(
      stopTime => {
        trips
          .filter(_.trip_id == stopTime.trip_id)
          .toList
          .headOption
          .foreach(tripRec => {
            val shortHeadSign = HeadSign.headSignToShortHeadsignMap(tripRec.trip_headsign)
            val minutes       = ChronoUnit.MINUTES.between(now, stopTime.departure.get)
            val set           = destinationsToTimesMap(shortHeadSign)
            destinationsToTimesMap += (shortHeadSign -> (set ++ minutes))
          })
      }
    )

    destinationsToTimesMap.keys
      .map(
        shortHeadSign => {
          shortHeadSign + " : " + destinationsToTimesMap(shortHeadSign).toList.sorted
            .map(_.toString)
            .reduce((acc, str) => s"$acc, $str")
        }
      )
      .toList
      .reduce(
        (a, b) => s"$a, $b"
      )
  }

  case class Response(body: String, headers: Map[String, String], statusCode: Int = 200) {
    def javaHeaders: java.util.Map[String, String] = headers.asJava
  }

}
