package sdk.model

import play.api.libs.json.{Json, Format}

case class Player(
  playerId: Int,
  totalEconomy: Int,
  totalIndustry: Int,
  totalScience: Int,
  aiControlled: Boolean,
  totalStars: Int,
  totalCarriers: Int,
  totalShips: Int,
  name: String,
  scanning: Tech,
  hyperspaceRange: Tech,
  terraforming: Tech,
  experimentation: Tech,
  weapons: Tech,
  banking: Tech,
  manufacturing: Tech,
//  conceded: ConcededResult.ConcededResult,
  ready: Boolean,
  missedTurns: Int,
  renownToGive: Int
)

case class Tech(
  value: Double,
  level: Int
)

//object ConcededResult extends Enumeration {
//  type ConcededResult = Value
//  val active, awayFromKeyboard, quit = Value
//
//  implicit val fmt: Format[ConcededResult] = Json.format[ConcededResult]
//}

object Player {
  implicit val fmt: Format[Player] = Json.format[Player]
}

object Tech {
  implicit val fmt: Format[Tech] = Json.format[Tech]
}