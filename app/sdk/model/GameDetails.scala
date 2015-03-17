package sdk.model

import play.api.libs.json._

case class GameDetails(
  turnBased: Boolean,
  turnBasedTimeout: Int,
  war: Boolean,
  tickRate: Int,
  productionRate: Int,
  totalStars: Int,
  starsForVictory: Int,
  tradeCost: Int,
  tradeScanned: Boolean,
  carrierSpeed: Double
)

object GameDetails {
  implicit val fmt: Format[GameDetails] = Json.format[GameDetails]
}
