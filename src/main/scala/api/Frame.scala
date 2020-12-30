package api

import spray.json.DefaultJsonProtocol

case class Frame(text: String, icon: String = "i21934", index: Int = 0)

object Frame extends DefaultJsonProtocol {
  implicit val frameFormat = jsonFormat3(Frame.apply)
}
