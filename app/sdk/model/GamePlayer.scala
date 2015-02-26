package sdk.model

import play.api.libs.json._

case class GamePlayer(
  playerId: Int,
  admin: Boolean
)

object GamePlayer {
  implicit val fmt: Format[GamePlayer] = Json.format[GamePlayer]
}
