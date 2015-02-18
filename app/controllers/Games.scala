package controllers

import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object Games extends Controller {

  def index = AuthenticatedAction.async { (request, client) =>
    val fGames = client.openGames()

    fGames.map { game =>
      Ok(Json.obj("result" -> game))
    }
  }
}
