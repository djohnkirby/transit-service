package handlers

import api.{Frame, Frames}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import gtfs.Station
import gtfs.parser.GtfsFileReader
import org.apache.commons.io.FileUtils
import spray.json._

import java.io.File
import java.time.{LocalDate, LocalDateTime}
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
    val stationIds = Station.stationIdMap.get(station)

    val awsCredentials = new BasicAWSCredentials("", "")

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
    ) //Working

    val gtfsFileReader = new GtfsFileReader(".")
    val stoptimes      = gtfsFileReader.getStopTimes
    /*
    //find all stoptimes in the next two hours for this particular station
    val upcomingStopsFromThisStation = stoptimes.filter(str => {
      val stopTime = str.toStopTime(dt = today, offset = walkTime)
      stationIds.contains(stopTime.stop.stop_id) &&
        stopTime.departure.isAfter(now) &&
        stopTime.departure.isBefore(now.plusHours(2))
    })
    s"Found ${upcomingStopsFromThisStation.size} stops from this station in the next 2 hours\n"*/

    s"Found ${stoptimes.size} stop times"
  }

  case class Response(body: String, headers: Map[String, String], statusCode: Int = 200) {
    def javaHeaders: java.util.Map[String, String] = headers.asJava
  }

}
