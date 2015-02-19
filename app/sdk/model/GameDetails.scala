package sdk.model

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

object GameDetails {
  implicit val gameDetailsWrites: Writes[GameDetails] = (
    (JsPath \ "turnBased").write[Boolean] and
      (JsPath \ "turnBasedTimeout").write[Int] and
      (JsPath \ "war").write[Boolean] and
      (JsPath \ "tickRate").write[Int] and
      (JsPath \ "productionRate").write[Int] and
      (JsPath \ "totalStars").write[Int] and
      (JsPath \ "starsForVictory").write[Int] and
      (JsPath \ "tradeCost").write[Int] and
      (JsPath \ "tradeScanned").write[Boolean] and
      (JsPath \ "fleetSpeed").write[Double]
    )(unlift(GameDetails.unapply))
}

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
                      fleetSpeed: Double
                        )