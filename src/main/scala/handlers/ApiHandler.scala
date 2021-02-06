package handlers

import api.{Frame, Frames}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import gtfs.parser.GtfsFileReader
import gtfs.{ScheduleFinder, Station}
import org.apache.commons.io.FileUtils
import spray.json._

import java.io.File
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
    val parameters = request.getQueryStringParameters
    val walkTime   = parameters.get("walkTime").toLong
    val station    = Station.withName(parameters.get("station"))

    val frame = getFrameMessage(station, walkTime)

    Response(Frames(Seq(Frame(frame))).toJson.toString, Map("Content-Type" -> "application/json"))
  }

  private def getFrameMessage(station: Station.Value, walkTime: Long): String = {

    //I have a station. I know what time it is. Now I need to find out
    //what trains are leaving soon and where they're going.
    val stationIds = Station.stationIdMap.get(station).getOrElse(Set.empty)

    if (stationIds.isEmpty)
      return "Error, no stationIDs found for this station"

    val awsCredentials = new BasicAWSCredentials(
      "",
      ""
    ) //DANIELTODO: DO NOT COMMIT THIS!!!!!!!

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

    FileUtils.copyInputStreamToFile(
      amazonS3Client.getObject("transitservice-data", "calendar.txt").getObjectContent,
      new File("calendar.txt")
    )

    FileUtils.copyInputStreamToFile(
      amazonS3Client.getObject("transitservice-data", "calendar_dates.txt").getObjectContent,
      new File("calendar_dates.txt")
    )

    val scheduleFinder = new ScheduleFinder(
      new ScheduleFinder.Dependencies {
        override val gtfsfileReader: GtfsFileReader = new GtfsFileReader(".")
      }
    )

    scheduleFinder.findSchedule(stationIds, walkTime)
  }

  case class Response(body: String, headers: Map[String, String], statusCode: Int = 200) {
    def javaHeaders: java.util.Map[String, String] = headers.asJava
  }

}
