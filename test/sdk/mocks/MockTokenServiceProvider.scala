package sdk.mocks

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import sdk.tokenService.TokenService
import sdk.{AuthCookie, AuthToken}

import scala.concurrent.{ExecutionContext, Future}

trait MockTokenServiceProvider extends MockitoSugar {
  protected def mockTokenService: TokenService = {
    val tokenService = mock[TokenService]

    val anyAuthToken: AuthToken = any[String].asInstanceOf[AuthToken] // Hack for matching value classes
    when(tokenService.lookupCookie(anyAuthToken)(any[ExecutionContext])).thenReturn(Future.successful(AuthCookie("test-cookie")))

    tokenService
  }
}
