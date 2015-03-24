package sdk.model

import play.api.data.validation.ValidationError
import play.api.libs.json._

import scala.util._

object Player {
  implicit val fmt: Format[Player] = Json.format[Player]
}

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
  scanning: PlayerTechLevel,
  hyperspaceRange: PlayerTechLevel,
  terraforming: PlayerTechLevel,
  experimentation: PlayerTechLevel,
  weapons: PlayerTechLevel,
  banking: PlayerTechLevel,
  manufacturing: PlayerTechLevel,
  conceded: PlayerConcededResult.ConcededResult,
  ready: Boolean,
  missedTurns: Int,
  renownToGive: Int
)

object PlayerTechLevel {
  implicit val fmt: Format[PlayerTechLevel] = Json.format[PlayerTechLevel]
}

case class PlayerTechLevel(
  value: Double,
  level: Int
)

object PlayerConcededResult extends Enumeration {
  type ConcededResult = Value
  val active, awayFromKeyboard, quit = Value

  implicit val fmt: Format[ConcededResult] = new Format[ConcededResult] {
    def reads(json: JsValue): JsResult[ConcededResult] = Try(PlayerConcededResult.withName(json.as[String])) match {
      case Success(result) => JsSuccess(result)
      case Failure(e) => JsError(ValidationError(e.getMessage))
    }

    def writes(o: ConcededResult): JsValue = JsString(o.toString)
  }
}