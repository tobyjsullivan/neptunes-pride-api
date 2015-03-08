package sdk.model

import play.api.libs.json._

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

object GameStatus {
  implicit val fmt: Format[GameStatus] = Json.format[GameStatus]
}
