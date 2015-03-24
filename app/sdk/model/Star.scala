package sdk.model

import play.api.libs.json.{Json, Format}

case class Star(
  starId: Int,
  name: String,
  playerId: Option[Int],
  visible: Boolean,
  position: Position,
  economy: Option[Int],
  industry: Option[Int],
  science: Option[Int],
  naturalResources: Option[Int],
  terraformedResources: Option[Int],
  warpGate: Option[Boolean],
  ships: Option[Int]
)

object Star {
  implicit val fmt: Format[Star] = Json.format[Star]
}
