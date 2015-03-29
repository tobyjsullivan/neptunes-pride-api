package sdk

import play.api.libs.json._
import sdk.http.impl.PlayWebService
import sdk.http.{RequestHolder, Response, WebService}
import sdk.model._
import sdk.tokenService.TokenService
import sdk.tokenService.impl.TokenServiceImpl

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object NPClient {
  val rootUrl = "http://triton.ironhelmet.com"
  val authServiceUrl = s"$rootUrl/arequest"
  val metadataServiceUrl = s"$rootUrl/mrequest"
  val gameServiceUrl = s"$rootUrl/grequest"

  case class PlayerInfo(games: List[GameMetadata])
  case class UniverseReport(game: Game, players: Seq[Player], stars: Seq[Star])

  def exchangeForAuthToken(username: String, password: String, ws: WebService = PlayWebService, ts: TokenService = TokenServiceImpl)(implicit ec: ExecutionContext): Future[AuthToken] = {
    for {
      oCookie <- fetchAuthCookie(username, password)(ws, ec)
      token <- ts.getToken(oCookie)
    } yield token
  }

  private def fetchAuthCookie(username: String, password: String)(implicit webServiceProvider: WebService, ec: ExecutionContext): Future[Option[AuthCookie]] = {
    val loginUrl = s"$authServiceUrl/login"

    val data = Map(
      "type" -> Seq("login"),
      "alias" -> Seq(username),
      "password" -> Seq(password)
    )

    postFormData(loginUrl, data, None).map { response =>
      for {
        authCookie <- response.cookie("auth")
        cookieValue <- authCookie.value
      } yield {
        AuthCookie(cookieValue)
      }
    }
  }

  private def postFormData(url: String, data: Map[String, Seq[String]], oCookie: Option[AuthCookie])(implicit webServiceProvider: WebService, ec: ExecutionContext): Future[Response] = {
    val holder: RequestHolder = webServiceProvider.url(url)
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8")

    val authedHolder = oCookie match {
      case Some(cookie) => holder.withHeaders("Cookie" -> s"auth=${cookie.value}")
      case None => holder
    }
    authedHolder.post(data)
  }
}

class NPClient(token: AuthToken)(implicit webServiceProvider: WebService = PlayWebService, tokenServiceProvider: TokenService = TokenServiceImpl) {
  import sdk.NPClient._
  import responseParsers.FullUniverseReportParsers._

  private val orderEndpointUrl = s"$gameServiceUrl/order"

  def getOpenGames()(implicit ec: ExecutionContext): Future[Seq[GameMetadata]] = {
    for {
      cookie <- tokenServiceProvider.lookupCookie(token)
      playerInfo <- fetchPlayerInfo(cookie)
    } yield playerInfo.games
  }

  def getGameDetails(gameId: Long)(implicit ec: ExecutionContext): Future[Game] = {
    for {
      cookie <- tokenServiceProvider.lookupCookie(token)
      universeReport <- fetchFullUniverseReport(gameId, cookie)
    } yield universeReport.game
  }

  def getPlayers(gameId: Long)(implicit ec: ExecutionContext): Future[Seq[Player]] = {
    for {
      cookie <- tokenServiceProvider.lookupCookie(token)
      universeReport <- fetchFullUniverseReport(gameId, cookie)
    } yield universeReport.players
  }

  def getStars(gameId: Long)(implicit ec: ExecutionContext): Future[Seq[Star]] = {
    for {
      cookie <- tokenServiceProvider.lookupCookie(token)
      universeReport <- fetchFullUniverseReport(gameId, cookie)
    } yield universeReport.stars
  }

  def submitTurn(gameId: Long)(implicit ec: ExecutionContext): Future[Unit] = {
    tokenServiceProvider.lookupCookie(token).flatMap { cookie =>
      val data = Map(
        "type" -> Seq("order"),
        "order" -> Seq("force_ready"),
        "version" -> Seq("7"),
        "game_number" -> Seq(gameId.toString)
      )

      postFormData(orderEndpointUrl, data, Some(cookie))
    }.map { _ =>
      () // Drop the response and return Unit
    }
  }

  private def fetchPlayerInfo(cookie: AuthCookie)(implicit ec: ExecutionContext): Future[PlayerInfo] = {
    val initEndpointUrl = s"$metadataServiceUrl/init_player"

    val data = Map(
      "type" -> Seq("init_player")
    )

    postFormData(initEndpointUrl, data, Some(cookie)).map { response =>
      val jsGames = (response.json \\ "open_games").head.as[JsArray]

      val games = jsGames.value.map { jsonGame =>
        GameMetadata(
          gameId = (jsonGame \ "number").as[String].toLong,
          name = (jsonGame \ "name").as[String]
        )
      }

      PlayerInfo(games.toList)
    }
  }

  private def fetchFullUniverseReport(gameId: Long, cookie: AuthCookie)(implicit ec: ExecutionContext): Future[UniverseReport] = {
    val data = Map(
      "type" -> Seq("order"),
      "order" -> Seq("full_universe_report"),
      "version" -> Seq("7"),
      "game_number" -> Seq(gameId.toString)
    )

    postFormData(orderEndpointUrl, data, Some(cookie)).map { response =>
      (response.json \ "report").as[UniverseReport]
    }
  }
}
