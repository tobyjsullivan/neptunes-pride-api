package sdk.model

import play.api.libs.json._

case class Game(
  gameId: Long,
  name: String,
  details: Option[GameDetails] = None,
  status: Option[GameStatus] = None,
  player: Option[GamePlayer] = None
)

object Game {
  implicit val fmt: Format[Game] = Json.format[Game]
}
