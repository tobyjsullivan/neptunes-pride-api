package sdk.model

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Game {
  implicit val gameWrites: Writes[Game] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "number").write[String] and
      (JsPath \ "version").write[Int]
    )(unlift(Game.unapply))
}

case class Game(name: String, number: String, version: Int)
