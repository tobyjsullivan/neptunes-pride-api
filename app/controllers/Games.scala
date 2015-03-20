package controllers

import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

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

  def readPlayers(gameId: Long) = AuthenticatedAction.async { (request, client) =>
    val fPlayerDetails = client.getPlayerDetails(gameId)

    fPlayerDetails.map { players =>
      Ok(Json.obj("result" -> players))
    }
  }

  def submitTurn(gameId: Long) = AuthenticatedAction.async { (request, client) =>
    val fSubmitRequest = client.submitTurn(gameId)

    fSubmitRequest.map { _ =>
      Ok(Json.obj("result" -> "ok"))
    }
  }
}
