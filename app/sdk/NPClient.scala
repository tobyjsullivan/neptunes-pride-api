package sdk

import play.api.libs.json.{JsValue, JsArray}
import play.api.libs.ws.{WSResponse, WS, WSRequestHolder}
import play.api.Play.current
import sdk.model.{GamePlayer, GameStatus, GameDetails, Game}
import sdk.tokenService.TokenService

import scala.concurrent.{ExecutionContext, Future}

object NPClient {
  val rootUrl = "http://triton.ironhelmet.com"
  val authServiceUrl = rootUrl + "/arequest"
  val metadataServiceUrl = rootUrl + "/mrequest"
  val gameServiceUrl = rootUrl + "/grequest"

  case class PlayerInfo(games: List[Game])
  case class UniverseReport(game: Game)

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
  import NPClient._

  def getOpenGames()(implicit ec: ExecutionContext): Future[List[Game]] = {
    for(
      cookie <- TokenService.lookupCookie(token);
      playerInfo <- fetchPlayerInfo(cookie)
    ) yield playerInfo.games
  }

  def getGameDetails(gameId: Long)(implicit ec: ExecutionContext): Future[Game] = {
    for(
      cookie <- TokenService.lookupCookie(token);
      universeReport <- fetchFullUniverseReport(gameId, cookie)
    ) yield universeReport.game
  }

  private def fetchPlayerInfo(cookie: AuthCookie)(implicit ec: ExecutionContext): Future[PlayerInfo] = {
    val initEndpointUrl = NPClient.metadataServiceUrl + "/init_player"

    val data = Map(
      "type" -> Seq("init_player")
    )

    val fResponse = NPClient.postFormData(initEndpointUrl, data, Some(cookie))

    fResponse.map { response =>
      val jsGames = (response.json \\ "open_games").head.as[JsArray]


      val games = jsGames.value.toList.map { jsonGame =>
        val name = (jsonGame \ "name").as[String]
        val gameId = (jsonGame \ "number").as[String].toLong

        Game(
          gameId = gameId,
          name = name,
          details = None,
          status = None,
          player = None
        )
      }

      PlayerInfo(games)
    }
  }

  private def fetchFullUniverseReport(gameId: Long, cookie: AuthCookie)(implicit ec: ExecutionContext): Future[UniverseReport] = {
    val orderEndpointUrl = NPClient.gameServiceUrl + "/order"

    val data = Map(
      "type" -> Seq("order"),
      "order" -> Seq("full_universe_report"),
      "version" -> Seq("7"),
      "game_number" -> Seq(gameId.toString)
    )

    val fResponse = NPClient.postFormData(orderEndpointUrl, data, Some(cookie))

    fResponse.map { response =>
      val jsReport = response.json \ "report"

      val gameName = (jsReport \ "name").as[String]
      val gameDetails = parseGameDetails(jsReport)
      val gameStatus = parseGameStatus(jsReport)
      val gamePlayer = parseGamePlayer(jsReport)

      val game = Game(
        gameId = gameId,
        name = gameName,
        details = Some(gameDetails),
        status = Some(gameStatus),
        player = Some(gamePlayer)
      )

      UniverseReport(game)
    }
  }

  private def parseGameDetails(jsReport: JsValue): GameDetails =
    GameDetails(
      turnBased = (jsReport \ "turn_based").as[Int] != 0,
      turnBasedTimeout = (jsReport \ "turn_based_time_out").as[Int],
      war = (jsReport \ "war").as[Int] != 0,
      tickRate = (jsReport \ "tick_rate").as[Int],
      productionRate = (jsReport \ "production_rate").as[Int],
      totalStars = (jsReport \ "total_stars").as[Int],
      starsForVictory = (jsReport \ "stars_for_victory").as[Int],
      tradeCost = (jsReport \ "trade_cost").as[Int],
      tradeScanned = (jsReport \ "trade_scanned").as[Int] != 0,
      fleetSpeed = (jsReport \ "fleet_speed").as[Double]
    )

  private def parseGameStatus(jsReport: JsValue): GameStatus =
    GameStatus(
      startTime = (jsReport \ "start_time").as[Long],
      now = (jsReport \ "now").as[Long],
      started = (jsReport \ "started").as[Boolean],
      paused = (jsReport \ "paused").as[Boolean],
      gameOver = (jsReport \ "game_over").as[Int] != 0,
      productions = (jsReport \ "productions").as[Int],
      productionCounter = (jsReport \ "production_counter").as[Int],
      tick = (jsReport \ "tick").as[Int],
      tickFragment = (jsReport \ "tick_fragment").as[Double]
    )

  private def parseGamePlayer(jsReport: JsValue): GamePlayer =
    GamePlayer(
      playerId = (jsReport \ "player_uid").as[Int],
      admin = (jsReport \ "admin").as[Int] > 0
    )
}
