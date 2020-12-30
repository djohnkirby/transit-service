package handlers

import api.{Frame, Frames}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
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
    Response(Frames(Seq(Frame(s"Under construction\n"))).toJson.toString, Map("Content-Type" -> "application/json"))
  }

  case class Response(body: String, headers: Map[String,String], statusCode: Int = 200) {
    def javaHeaders: java.util.Map[String, String] = headers.asJava
  }

}
