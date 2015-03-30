package controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import sdk.model.{CarrierOrder, CarrierOrderAction => OrderAction}
import sdk.model.CarrierOrderAction._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

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

  def readCarriers(gameId: Long) = AuthenticatedAction.async { (request, client) =>
    val fCarriers = client.getCarriers(gameId)

    fCarriers.map { carriers =>
      Ok(Json.obj("result" -> carriers))
    }
  }

  def submitTurn(gameId: Long) = AuthenticatedAction.async { (request, client) =>
    val fSubmitRequest = client.submitTurn(gameId)

    fSubmitRequest.map {
      case Left(errorString) => BadRequest(Json.obj("error" -> errorString))
      case Right(carrier) => Ok(Json.obj("result" -> "ok"))
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

  def issueOrder(gameId: Long, carrierId: Int) = AuthenticatedAction.async { (request, client) =>
    request.body.asJson match {
      case None => Future.successful(BadRequest(Json.obj("error" -> "Request content must be JSON")))
      case Some(jsRequest) => {
        val sAction = (jsRequest \ "action").as[String]

        Try(OrderAction.withName(sAction)) match {
          case Success(parsedAction) => {
            val starId: Int = (jsRequest \ "starId").as[Int]
            val action: CarrierOrderAction = parsedAction
            val ships: Int = (jsRequest \ "ships").asOpt[Int].getOrElse(0)
            val delay: Int = (jsRequest \ "delay").asOpt[Int].getOrElse(0)

            val order = CarrierOrder(delay, starId, action, ships)

            client.issueOrders(gameId, carrierId, Seq(order)).map {
              case Left(errorString) => BadRequest(Json.obj("error" -> errorString))
              case Right(carrier) => Ok(Json.obj("result" -> order))
            }
          }
          case Failure(_) => Future.successful(BadRequest(Json.obj("error" -> "Specified action is invalid")))
        }
      }
    }
  }
}
