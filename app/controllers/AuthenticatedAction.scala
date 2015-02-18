package controllers

import controllers.Authentication._
import play.api.libs.json.Json
import play.api.mvc._
import sdk.{NPClient, AuthToken}

import scala.concurrent._

object AuthenticatedAction {
  private def extractToken[T](request: Request[T]): Option[AuthToken] = {
    request.headers.get("X-Auth-Token").map(AuthToken)
  }

  private val failureResponse =
    Unauthorized(Json.obj(
      "error" -> Json.obj(
        "message" -> "Login failed."
      )
    ))

  def apply[T](block: (Request[AnyContent], NPClient) => Result): Action[AnyContent] = Action { request =>
    val oToken = extractToken(request)
    oToken.map { token =>
      block(request, new NPClient(token))
    }.getOrElse {
      failureResponse
    }
  }

  def async[T](block: (Request[AnyContent], NPClient) => Future[Result])(implicit ec: ExecutionContext): Action[AnyContent] = Action.async { request =>
    val oToken = extractToken(request)
    oToken.map { token =>
      block(request, new NPClient(token))
    }.getOrElse {
      Future(failureResponse)
    }
  }
}
