package sdk

import play.api.libs.ws.{WS, WSRequestHolder}
import play.api.Play.current
import sdk.tokenService.TokenService

import scala.concurrent.{ExecutionContext, Future}

object NPClient {
  val loginUrl = "http://triton.ironhelmet.com/arequest/login"

  def exchangeForAuthToken(username: String, password: String)(implicit ec: ExecutionContext): Future[AuthToken] = {
    for (
      oCookie <- fetchAuthCookie(username, password);
      cookie <- Future(oCookie.get);
      token <- TokenService.getToken(cookie)
    ) yield token
  }

  private def fetchAuthCookie(username: String, password: String)(implicit ec: ExecutionContext): Future[Option[AuthCookie]] = {
    val holder: WSRequestHolder = WS.url(loginUrl)
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
}

class NPClient(token: AuthToken) {

}
