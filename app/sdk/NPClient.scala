package sdk

import play.api.libs.json.JsArray
import play.api.libs.ws.{WSResponse, WS, WSRequestHolder}
import play.api.Play.current
import sdk.model.Game
import sdk.tokenService.TokenService

import scala.concurrent.{ExecutionContext, Future}

object NPClient {
  val rootUrl = "http://triton.ironhelmet.com"
  val authServiceUrl = rootUrl + "/arequest"
  val metadataServiceUrl = rootUrl + "/mrequest"
  val gameServiceUrl = rootUrl + "/grequest"

  def exchangeForAuthToken(username: String, password: String)(implicit ec: ExecutionContext): Future[AuthToken] = {
    for (
      oCookie <- fetchAuthCookie(username, password);
      cookie <- Future(oCookie.get);
      token <- TokenService.getToken(cookie)
    ) yield token
  }

  private def fetchAuthCookie(username: String, password: String)(implicit ec: ExecutionContext): Future[Option[AuthCookie]] = {
    val loginUrl = authServiceUrl+"/login"

    val data = Map(
      "type" -> Seq("login"),
      "alias" -> Seq(username),
      "password" -> Seq(password)
    )

    val fResponse = postFormData(loginUrl, data, None)

    fResponse.map { response =>
      val oAuthCookie = response.cookie("auth")

      oAuthCookie.flatMap(_.value).map { cookieValue =>
        AuthCookie(cookieValue)
      }
    }
  }

  private def postFormData(url: String, data: Map[String, Seq[String]], oCookie: Option[AuthCookie]): Future[WSResponse] = {
    val holder: WSRequestHolder = WS.url(url)
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8")

    val authedHolder =
      if (oCookie.isDefined)
        holder.withHeaders("Cookie" -> ("auth="+oCookie.get.value))
      else
        holder

    authedHolder.post(data)
  }
}

class NPClient(token: AuthToken) {
  def openGames()(implicit ec: ExecutionContext): Future[List[Game]] = {
    for(
      cookie <- TokenService.lookupCookie(token);
      games <- fetchOpenGames(cookie)
    ) yield games
  }

  private def fetchOpenGames(cookie: AuthCookie)(implicit ec: ExecutionContext): Future[List[Game]] = {
    val initEndpointUrl = NPClient.metadataServiceUrl + "/init_player"

    val data = Map(
      "type" -> Seq("init_player")
    )

    val fResponse = NPClient.postFormData(initEndpointUrl, data, Some(cookie))

    fResponse.map { response =>
      val jsGames = (response.json \\ "open_games").head.as[JsArray]


      jsGames.value.toList.map { jsonGame =>
        val name = (jsonGame \ "name").as[String]
        val number = (jsonGame \ "number").as[String]
        val version = (jsonGame \ "version").as[String].toInt

        Game(name, number, version)
      }

    }
  }
}
