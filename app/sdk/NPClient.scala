package sdk

import play.api.libs.json._
import sdk.http.impl.PlayWebService
import sdk.http.{RequestHolder, Response, WebService}
import sdk.model.CarrierOrderAction.CarrierOrderAction
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
  case class UniverseReport(game: Game, players: Seq[Player], stars: Seq[Star], carriers: Seq[Carrier])

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

  private def postFormDataAndCheckForError(url: String, data: Map[String, Seq[String]], oCookie: Option[AuthCookie])(implicit webServiceProvider: WebService, ec: ExecutionContext): Future[Either[String, JsValue]] = {
    postFormData(url, data, oCookie).map { resp =>
      if (resp.status != 200)
        Left("Unexpected status code from the game: "+resp.status)
      else parseErrorOrElse(resp.json, resp.json)
    }
  }

  private def parseErrorOrElse[A](jsResponse: JsValue, orElse: => A): Either[String, A] =
    if ((jsResponse \ "event").asOpt[String] == Some("order:error")) Left((jsResponse \ "report").as[String])
    else Right(orElse)

  private def parseOrError[A](jsResponse: JsValue)(implicit fjs: Reads[A]): Either[String, A] =
    (jsResponse \ "report").validate[A] match {
      case JsSuccess(x, _) => Right(x)
      case JsError(parseError) => Left("Unknown response parse error. ParseError: "+parseError.flatMap(_._2).map(_.message) +"; Data: "+jsResponse.toString)
    }
}

class NPClient(token: AuthToken)(implicit webServiceProvider: WebService = PlayWebService, tokenServiceProvider: TokenService = TokenServiceImpl) {
  import sdk.NPClient._
  import responseParsers.FullUniverseReportParsers._
  import responseParsers.CarrierParsers._

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

  def getCarriers(gameId: Long)(implicit ec: ExecutionContext): Future[Seq[Carrier]] = {
    for {
      cookie <- tokenServiceProvider.lookupCookie(token)
      universeReport <- fetchFullUniverseReport(gameId, cookie)
    } yield universeReport.carriers
  }

  def createCarrier(gameId: Long, starId: Int, ships: Int)(implicit ec: ExecutionContext): Future[Either[String, Carrier]] = {
    tokenServiceProvider.lookupCookie(token).flatMap { cookie =>
      val order = Seq("new_fleet", starId.toString, ships.toString).mkString(",")

      val data = Map(
        "type" -> Seq("order"),
        "order" -> Seq(order),
        "version" -> Seq("7"),
        "game_number" -> Seq(gameId.toString)
      )

      postFormDataAndCheckForError(orderEndpointUrl, data, Some(cookie)).map {
        case Right(jsCarrier) => parseOrError[Carrier](jsCarrier)
        case Left(e) => Left(e)
      }
    }
  }

  def issueOrders(gameId: Long, carrierId: Int, orders: Seq[CarrierOrder])(implicit ec: ExecutionContext): Future[Either[String, Unit]] = {
    tokenServiceProvider.lookupCookie(token).flatMap { cookie =>
      def getActionId(action: CarrierOrderAction): Int = action match {
        case CarrierOrderAction.DoNothing => 0
        case CarrierOrderAction.CollectAll => 1
        case CarrierOrderAction.DropAll => 2
        case CarrierOrderAction.Collect => 3
        case CarrierOrderAction.Drop => 4
        case CarrierOrderAction.CollectAllBut => 5
        case CarrierOrderAction.DropAllBut => 6
        case CarrierOrderAction.Garrison => 7
      }

      def buildOrderString: String = {
        val sDelay = orders.map(_.delay).mkString("_")
        val sStarId = orders.map(_.starId).mkString("_")
        val sAction = orders.map(_.action).map(getActionId).mkString("_")
        val sShips = orders.map(_.ships).mkString("_")

        Seq(sDelay, sStarId, sAction, sShips).mkString(",")
      }

      val sOrder = Seq("add_fleet_orders", carrierId, buildOrderString, 0).mkString(",")

      val data = Map(
        "type" -> Seq("order"),
        "order" -> Seq(sOrder),
        "version" -> Seq("7"),
        "game_number" -> Seq(gameId.toString)
      )

      postFormDataAndCheckForError(orderEndpointUrl, data, Some(cookie)) map {
        case Right(_) => Right(())
        case Left(e) => Left(e)
      }
    }
  }

  def submitTurn(gameId: Long)(implicit ec: ExecutionContext): Future[Either[String, Unit]] = {
    tokenServiceProvider.lookupCookie(token).flatMap { cookie =>
      val data = Map(
        "type" -> Seq("order"),
        "order" -> Seq("force_ready"),
        "version" -> Seq("7"),
        "game_number" -> Seq(gameId.toString)
      )

      postFormDataAndCheckForError(orderEndpointUrl, data, Some(cookie)) map {
        case Right(_) => Right(())
        case Left(e) => Left(e)
      }
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
