package controllers

import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import sdk.NPClient

import scala.concurrent.ExecutionContext.Implicits.global

object Authentication extends Controller {
  case class LoginData(username: String, password: String)

  val loginForm = Form(
    mapping(
      "username" -> text,
      "password" -> text
    )(LoginData.apply)(LoginData.unapply)
  )

  def login = Action.async { implicit request =>
    val loginData = loginForm.bindFromRequest.get

    val fAuthToken = NPClient.exchangeForAuthToken(loginData.username, loginData.password)

    fAuthToken.map { token =>
      val jsonResult = Json.obj(
        "auth-token" -> token.token
      )

      Ok(jsonResult)
    }.recover {
      case _ =>
        Unauthorized(Json.obj(
          "error" -> Json.obj(
            "message" -> "Login failed."
          )
        ))
    }

  }

}