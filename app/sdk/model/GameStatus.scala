package sdk.model

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

object GameStatus {
  implicit val gameStatusWrites: Writes[GameStatus] = (
    (JsPath \ "startTime").write[Long] and
      (JsPath \ "now").write[Long] and
      (JsPath \ "started").write[Boolean] and
      (JsPath \ "paused").write[Boolean] and
      (JsPath \ "gameOver").write[Boolean] and
      (JsPath \ "productions").write[Int] and
      (JsPath \ "productionCounter").write[Int] and
      (JsPath \ "tick").write[Int] and
      (JsPath \ "tickFragment").write[Double]
    )(unlift(GameStatus.unapply))
}

case class GameStatus(
                     startTime: Long,
                     now: Long,
                     started: Boolean,
                     paused: Boolean,
                     gameOver: Boolean,
                     productions: Int,
                     productionCounter: Int,
                     tick: Int,
                     tickFragment: Double
                       )