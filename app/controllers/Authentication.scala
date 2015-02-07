package controllers

import actors._
import akka.pattern.ask
import akka.util.Timeout
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json
import play.api.libs.ws._
import play.api.mvc._
import play.api.Play.current

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Authentication extends Controller {
  val url = "http://triton.ironhelmet.com/arequest/login"

  val authActor = Akka.system.actorOf(AuthActor.props)

  case class LoginData(username: String, password: String)

  val loginForm = Form(
    mapping(
      "username" -> text,
      "password" -> text
    )(LoginData.apply)(LoginData.unapply)
  )

  private def fetchAuthCookie(username: String, password: String): Future[Option[AuthCookie]] = {
    val holder: WSRequestHolder = WS.url(url)
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8")

    val fResponse = holder.post(
      Map(
        "type" -> Seq("login"),
        "alias" -> Seq(username),
        "password" -> Seq(password)
      )
    )

    fResponse.map { response =>
      val oAuthCookie = response.cookie("auth")

      oAuthCookie.flatMap(_.value).map { cookieValue =>
        AuthCookie(cookieValue)
      }
    }
  }

  private def exchangeCookieForToken(cookie: AuthCookie): Future[AuthToken] = {
    implicit val timeout = Timeout(200 millis)

    (authActor ? GenerateToken(cookie)).mapTo[AuthToken]
  }

  def login = Action.async { implicit request =>
    val loginData = loginForm.bindFromRequest.get

    val foAuthCookie: Future[Option[AuthCookie]] = fetchAuthCookie(loginData.username, loginData.password)

    val fofAuthToken: Future[Option[Future[AuthToken]]] = foAuthCookie.map {
      _.map {
        case cookie: AuthCookie => exchangeCookieForToken(cookie)
      }
    }

    val fResponse = fofAuthToken.flatMap {
      case None =>
        Future {
          Unauthorized(Json.obj(
            "error" -> Json.obj(
              "message" -> "Login failed."
            )
          ))
        }
      case Some(fToken) =>
        fToken.map { token =>
          val jsonResult = Json.obj(
            "auth-token" -> token.token
          )

          Ok(jsonResult)
        }
    }

    fResponse
  }

}