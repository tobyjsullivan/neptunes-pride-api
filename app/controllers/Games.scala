package controllers

import play.api.libs.json.{JsNull, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Games extends Controller {

  def index = AuthenticatedAction.async { (request, client) =>
    val fGames = client.getOpenGames()

    fGames.map { game =>
      Ok(Json.obj("result" -> game))
    }
  }

  def read(gameId: Long) = AuthenticatedAction.async { (request, client) =>
    val fGameDetails = client.getGameDetails(gameId)

    fGameDetails.map { game =>
      Ok(Json.obj("result" -> game))
    }
  }

  def submitTurn(gameId: Long) = AuthenticatedAction.async { (request, client) =>
    val fSubmitRequest = client.submitTurn(gameId)

    fSubmitRequest.map { ok =>
      Ok(Json.obj("result" -> ok))
    }
  }
}
