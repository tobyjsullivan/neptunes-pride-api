package sdk

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mock.MockitoSugar
import sdk.mocks.{MockTokenServiceProvider, MockWebServiceProvider}

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global

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

    client.submitTurn(1374710347L)

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
  }
}
