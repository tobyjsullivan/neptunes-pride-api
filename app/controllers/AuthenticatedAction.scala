package controllers

import controllers.Authentication._
import play.api.libs.json.Json
import play.api.mvc._
import sdk.{NPClient, AuthToken}
import sdk.exception.InvalidTokenException

import scala.concurrent._
import scala.util.Try

object AuthenticatedAction {
  private def extractToken[T](request: Request[T]): Option[AuthToken] = {
    request.headers.get("X-Auth-Token").map(AuthToken)
  }

  private val responseTokenRequired =
    Unauthorized(Json.obj(
      "error" -> Json.obj(
        "message" -> "You must provide a valid auth token in the X-Auth-Token header."
      )
    ))

  private val responseInvalidToken =
    Unauthorized(Json.obj(
      "error" -> Json.obj(
        "message" -> "The provided auth token is invalid."
      )
    ))

  def apply[T](block: (Request[AnyContent], NPClient) => Result): Action[AnyContent] = Action { request =>
    val oToken = extractToken(request)
    oToken.map { token =>
      val tResult = Try(block(request, new NPClient(token)))
      tResult.recover {
        case e: InvalidTokenException =>
          responseInvalidToken
      }.get
    }.getOrElse {
      responseTokenRequired
    }
  }

  def async[T](block: (Request[AnyContent], NPClient) => Future[Result])(implicit ec: ExecutionContext): Action[AnyContent] = Action.async { request =>
    val oToken = extractToken(request)
    oToken.map { token =>
      block(request, new NPClient(token)).recover {
        case e: InvalidTokenException =>
          responseInvalidToken
      }
    }.getOrElse {
      Future(responseTokenRequired)
    }
  }
}
