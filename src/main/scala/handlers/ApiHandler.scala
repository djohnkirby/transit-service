package handlers

import java.time.{LocalDate, LocalDateTime}

import api.{Frame, Frames}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import gtfs.Station
import gtfs.parser.GtfsFileReader
import spray.json._

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
    val today      = LocalDate.now()
    val now        = LocalDateTime.now()
    //I have a station. I know what time it is. Now I need to find out
    //what trains are leaving soon and where they're going.
    val stationIds = Station.stationIdMap.get(station)

    val gtfsFileReader = new GtfsFileReader("./transit_data/path-nj-us")
    val stoptimes = gtfsFileReader.getStopTimes

    //find all stoptimes in the next two hours for this particular station
    val upcomingStopsFromThisStation = stoptimes.filter(str => {
      val stopTime = str.toStopTime(dt = today, offset = walkTime)
      stationIds.contains(stopTime.stop.stop_id) &&
        stopTime.departure.isAfter(now) &&
        stopTime.departure.isBefore(now.plusHours(2))
    })
    s"Found ${upcomingStopsFromThisStation.size} stops from this station in the next 2 hours\n"
  }

  case class Response(body: String, headers: Map[String,String], statusCode: Int = 200) {
    def javaHeaders: java.util.Map[String, String] = headers.asJava
  }

}
