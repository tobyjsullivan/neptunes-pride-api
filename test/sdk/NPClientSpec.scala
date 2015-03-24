package sdk

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import sdk.mocks.{MockTokenServiceProvider, MockWebServiceProvider}
import sdk.model.{Position, PlayerConcededResult, PlayerTechLevel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.io.Source

class NPClientSpec extends FlatSpec with Matchers with MockitoSugar with MockTokenServiceProvider with MockWebServiceProvider {
  "submitTurn" should "send a post command to the server" in {
    // Setup the TokenService mock
    val tokenService = mockTokenService
    val anyToken = any[String].asInstanceOf[AuthToken]
    when(tokenService.lookupCookie(anyToken)(any[ExecutionContext])).thenReturn(Future.successful(AuthCookie("test-cookie-19634")))

    // Setup the WebService mocks
    val webService = mockWebService
    val requestHolder = mockRequestHolder
    when(webService.url(any[String])).thenReturn(requestHolder)

    val token = AuthToken("alksjdhfklajhs")
    val client = new NPClient(token)(webService, tokenService)

    val result = client.submitTurn(1374710347L)

    // Verify the correct URL was hit
    verify(webService).url("http://triton.ironhelmet.com/grequest/order")

    // Verify the correct content type was sent
    verify(requestHolder).withHeaders("Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8")

    // Verify the correct cookie was sent
    verify(requestHolder).withHeaders("Cookie" -> "auth=test-cookie-19634")

    // Verify the http method was POST and correct data was included
    verify(requestHolder).post(Map(
      "type" -> Seq("order"),
      "order" -> Seq("force_ready"),
      "version" -> Seq("7"),
      "game_number" -> Seq("1374710347")
    ))

    Await.ready(result, 500 millis)
  }

  "getPlayers" should "return a parsed summary of all players in the game" in {
    // Load mock report response
    val mockJsonResponseFile = "/full_universe_report001.json"
    val source = Source.fromURL(getClass.getResource(mockJsonResponseFile))
    val mockJsonResponse: String = source.mkString

    // Setup the TokenService mock
    val tokenService = mockTokenService
    val anyToken = any[String].asInstanceOf[AuthToken]
    when(tokenService.lookupCookie(anyToken)(any[ExecutionContext])).thenReturn(Future.successful(AuthCookie("test-cookie-19634")))

    // Setup the WebService mocks
    val webService = mockWebService
    val requestHolder = mockRequestHolder
    val response = mockResponse
    when(webService.url(any[String])).thenReturn(requestHolder)
    when(requestHolder.post(any[Map[String, Seq[String]]])(any[ExecutionContext])).thenReturn(Future.successful(response))
    when(response.json).thenReturn(Json.parse(mockJsonResponse))

    val token = AuthToken("alksjdhfklajhs")
    val client = new NPClient(token)(webService, tokenService)

    val result = client.getPlayers(98734811L)

    Await.ready(result, 500 millis)

    // Verify the correct URL was hit
    verify(webService).url("http://triton.ironhelmet.com/grequest/order")

    // Verify the http method was POST and full universe report was requested
    verify(requestHolder).post(Map(
      "type" -> Seq("order"),
      "order" -> Seq("full_universe_report"),
      "version" -> Seq("7"),
      "game_number" -> Seq("98734811")
    ))

    result.isCompleted shouldBe true
    val players  = result.value.get.get
    players.length shouldBe 8
    val firstPlayer = players(0)
    val secondPlayer = players(1)

    firstPlayer.playerId shouldBe 0
    firstPlayer.totalEconomy shouldBe 28
    firstPlayer.totalIndustry shouldBe 19
    firstPlayer.totalScience shouldBe 6
    firstPlayer.aiControlled shouldBe true
    firstPlayer.totalStars shouldBe 10
    firstPlayer.totalCarriers shouldBe 8
    firstPlayer.totalShips shouldBe 1270
    firstPlayer.name shouldBe "Annimus"
    firstPlayer.scanning shouldBe PlayerTechLevel(0.5, 2)
    firstPlayer.hyperspaceRange shouldBe PlayerTechLevel(0.875, 4)
    firstPlayer.terraforming shouldBe PlayerTechLevel(3, 3)
    firstPlayer.experimentation shouldBe PlayerTechLevel(360, 3)
    firstPlayer.weapons shouldBe PlayerTechLevel(5, 5)
    firstPlayer.banking shouldBe PlayerTechLevel(2, 2)
    firstPlayer.manufacturing shouldBe PlayerTechLevel(4, 4)
    firstPlayer.conceded shouldBe PlayerConcededResult.awayFromKeyboard
    firstPlayer.ready shouldBe true
    firstPlayer.missedTurns shouldBe 0
    firstPlayer.renownToGive shouldBe 8

    secondPlayer.playerId shouldBe 1
    secondPlayer.totalEconomy shouldBe 165
    secondPlayer.totalIndustry shouldBe 183
    secondPlayer.totalScience shouldBe 48
    secondPlayer.aiControlled shouldBe false
    secondPlayer.totalStars shouldBe 57
    secondPlayer.totalCarriers shouldBe 18
    secondPlayer.totalShips shouldBe 6558
    secondPlayer.name shouldBe "mrnich04"
    secondPlayer.scanning shouldBe PlayerTechLevel(0.75, 4)
    secondPlayer.hyperspaceRange shouldBe PlayerTechLevel(1.0, 5)
    secondPlayer.terraforming shouldBe PlayerTechLevel(6, 6)
    secondPlayer.experimentation shouldBe PlayerTechLevel(720, 6)
    secondPlayer.weapons shouldBe PlayerTechLevel(7, 7)
    secondPlayer.banking shouldBe PlayerTechLevel(4, 4)
    secondPlayer.manufacturing shouldBe PlayerTechLevel(5, 5)
    secondPlayer.conceded shouldBe PlayerConcededResult.active
    secondPlayer.ready shouldBe false
    secondPlayer.missedTurns shouldBe 0
    secondPlayer.renownToGive shouldBe 8
  }
  "getStars" should "return a parsed summary of all stars visible to the current session" in {
    // Load mock report response
    val mockJsonResponseFile = "/full_universe_report001.json"
    val source = Source.fromURL(getClass.getResource(mockJsonResponseFile))
    val mockJsonResponse: String = source.mkString

    // Setup the TokenService mock
    val tokenService = mockTokenService
    val anyToken = any[String].asInstanceOf[AuthToken]
    when(tokenService.lookupCookie(anyToken)(any[ExecutionContext])).thenReturn(Future.successful(AuthCookie("test-cookie-19634")))

    // Setup the WebService mocks
    val webService = mockWebService
    val requestHolder = mockRequestHolder
    val response = mockResponse
    when(webService.url(any[String])).thenReturn(requestHolder)
    when(requestHolder.post(any[Map[String, Seq[String]]])(any[ExecutionContext])).thenReturn(Future.successful(response))
    when(response.json).thenReturn(Json.parse(mockJsonResponse))

    val token = AuthToken("alksjdhfklajhs")
    val client = new NPClient(token)(webService, tokenService)

    val result = client.getStars(98734811L)

    Await.ready(result, 500 millis)

    // Verify the correct URL was hit
    verify(webService).url("http://triton.ironhelmet.com/grequest/order")

    // Verify the http method was POST and full universe report was requested
    verify(requestHolder).post(Map(
      "type" -> Seq("order"),
      "order" -> Seq("full_universe_report"),
      "version" -> Seq("7"),
      "game_number" -> Seq("98734811")
    ))

    result.isCompleted shouldBe true
    val stars  = result.value.get.get
    stars.length shouldBe 171
    val star1 = stars(0)
    val star5 = stars(4)
    val star97 = stars(83)
    val star192 = stars(170)

    star1.starId shouldBe 1
    star1.name shouldBe "Phad"
    star1.playerId shouldBe Some(3)
    star1.visible shouldBe false
    star1.position shouldBe Position(-1.8154, 1.8028)
    star1.economy shouldBe None
    star1.industry shouldBe None
    star1.science shouldBe None
    star1.naturalResources shouldBe None
    star1.terraformedResources shouldBe None
    star1.warpGate shouldBe None
    star1.ships shouldBe None

    star5.starId shouldBe 5
    star5.name shouldBe "Biham"
    star5.playerId shouldBe Some(4)
    star5.visible shouldBe true
    star5.position shouldBe Position(0.4346, 0.5038)
    star5.economy shouldBe Some(8)
    star5.industry shouldBe Some(7)
    star5.science shouldBe Some(2)
    star5.naturalResources shouldBe Some(50)
    star5.terraformedResources shouldBe Some(60)
    star5.warpGate shouldBe Some(false)
    star5.ships shouldBe Some(98)

    star97.starId shouldBe 97
    star97.name shouldBe "Taygeta"
    star97.playerId shouldBe Some(5)
    star97.visible shouldBe false
    star97.position shouldBe Position(-1.9289, 1.6063)
    star97.economy shouldBe None
    star97.industry shouldBe None
    star97.science shouldBe None
    star97.naturalResources shouldBe None
    star97.terraformedResources shouldBe None
    star97.warpGate shouldBe None
    star97.ships shouldBe None

    star192.starId shouldBe 192
    star192.name shouldBe "Electra"
    star192.playerId shouldBe Some(2)
    star192.visible shouldBe false
    star192.position shouldBe Position(0.0277, -0.1431)
    star192.economy shouldBe None
    star192.industry shouldBe None
    star192.science shouldBe None
    star192.naturalResources shouldBe None
    star192.terraformedResources shouldBe None
    star192.warpGate shouldBe None
    star192.ships shouldBe None
  }
}
