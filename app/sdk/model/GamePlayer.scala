package sdk.model

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

object GamePlayer {
  implicit val gamePlayerWrites: Writes[GamePlayer] = (
    (JsPath \ "playerId").write[Int] and
      (JsPath \ "admin").write[Boolean]
    )(unlift(GamePlayer.unapply))
}

case class GamePlayer(
                     playerId: Int,
                     admin: Boolean
                       )
