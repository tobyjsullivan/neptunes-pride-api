package sdk.model

import play.api.libs.json._

case class Game(
  name: String,
  details: Option[GameDetails],
  status: Option[GameStatus],
  player: Option[GamePlayer]
)

object Game {
  implicit val fmt: Format[Game] = Json.format[Game]
}
