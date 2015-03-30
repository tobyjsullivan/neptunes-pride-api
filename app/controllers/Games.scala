package controllers

import play.api.libs.json.Json
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

  def readPlayers(gameId: Long) = AuthenticatedAction.async { (request, client) =>
    val fPlayerDetails = client.getPlayers(gameId)

    fPlayerDetails.map { players =>
      Ok(Json.obj("result" -> players))
    }
  }

  def readStars(gameId: Long) = AuthenticatedAction.async { (request, client) =>
    val fStars = client.getStars(gameId)

    fStars.map { stars =>
      Ok(Json.obj("result" -> stars))
    }
  }

  def submitTurn(gameId: Long) = AuthenticatedAction.async { (request, client) =>
    val fSubmitRequest = client.submitTurn(gameId)

    fSubmitRequest.map { _ =>
      Ok(Json.obj("result" -> "ok"))
    }
  }

  def createCarrier(gameId: Long) = AuthenticatedAction.async { (request, client) =>
    request.body.asJson match {
      case None => Future.successful(BadRequest(Json.obj("error" -> "Request content must be JSON")))
      case Some(jsRequest) => {
        val starId: Int = (jsRequest \ "starId").as[Int]
        val ships: Int = (jsRequest \ "ships").as[Int]

        client.createCarrier(gameId, starId, ships).map {
          case Left(errorString) => BadRequest(Json.obj("error" -> errorString))
          case Right(carrier) => Ok(Json.obj("result" -> carrier))
        }
      }
    }
  }
}
