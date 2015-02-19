package sdk.model

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Game {
  implicit val gameWrites: Writes[Game] = (
    (JsPath \ "gameId").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "version").write[Int]
    )(unlift(Game.unapply))
}

case class Game(gameId: Long, name: String, version: Int)
