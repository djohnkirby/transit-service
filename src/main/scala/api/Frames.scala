package api

import spray.json.DefaultJsonProtocol

case class Frames(frames: Seq[Frame])

object Frames extends DefaultJsonProtocol {
  implicit val frameFormat = jsonFormat1(Frames.apply)
}
