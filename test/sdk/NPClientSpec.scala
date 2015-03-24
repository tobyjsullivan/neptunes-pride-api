package sdk

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import sdk.mocks.{MockTokenServiceProvider, MockWebServiceProvider}
import sdk.model.{PlayerConcededResult, PlayerTechLevel}

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

  "getPlayerDetails" should "return a json representation of all players in the game" in {
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

    val result = client.getPlayerDetails(98734811L)

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
    val firstPlayer = players.head

    firstPlayer.name should be ("Annimus")
    firstPlayer.totalStars shouldBe 10

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
    firstPlayer.manufacturing shouldBe PlayerTechLevel(4, 2)
    firstPlayer.conceded shouldBe PlayerConcededResult.awayFromKeyboard
    firstPlayer.ready shouldBe true
    firstPlayer.missedTurns shouldBe 0
    firstPlayer.renownToGive shouldBe 8
  }
}
