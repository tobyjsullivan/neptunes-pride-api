package sdk.model

import play.api.libs.json.{Json, Format}

case class Position(x: Double, y: Double)

object Position {
  implicit val fmt: Format[Position] = Json.format[Position]
}
