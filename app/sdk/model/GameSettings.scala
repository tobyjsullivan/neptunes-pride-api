package sdk.model

import play.api.libs.json._

case class GameSettings(
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

object GameSettings {
  implicit val fmt: Format[GameSettings] = Json.format[GameSettings]
}
