package sdk.model

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Game {
  import GameDetails._
  import GameStatus._
  import GamePlayer._

  implicit val gameWrites: Writes[Game] = (
    (JsPath \ "gameId").write[Long] and
      (JsPath \ "name").write[String] and
      JsPath.writeNullable[GameDetails] and
      JsPath.writeNullable[GameStatus] and
      JsPath.writeNullable[GamePlayer]
    )(unlift(Game.unapply))
}

case class Game(
                 gameId: Long,
                 name: String,
                 details: Option[GameDetails],
                 status: Option[GameStatus],
                 player: Option[GamePlayer]
                 )
