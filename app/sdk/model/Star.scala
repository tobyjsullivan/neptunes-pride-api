package sdk.model

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
