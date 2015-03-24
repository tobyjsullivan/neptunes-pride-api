package sdk.model

import play.api.libs.json.{Json, Format}

case class GameMetadata(gameId: Long, name: String)

object GameMetadata {
  implicit val fmt: Format[GameMetadata] = Json.format[GameMetadata]
}

