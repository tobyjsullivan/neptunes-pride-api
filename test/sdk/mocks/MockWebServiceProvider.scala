package sdk.mocks

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsNull
import sdk.http.{Cookie, RequestHolder, Response, WebService}

import scala.concurrent.{ExecutionContext, Future}

trait MockWebServiceProvider extends MockitoSugar {
  protected def mockWebService: WebService = {
    val webService = mock[WebService]

    val requestHolder = mockRequestHolder
    when(webService.url(any[String])).thenReturn(requestHolder)

    webService
  }

  protected def mockRequestHolder: RequestHolder = {
    val requestHolder = mock[RequestHolder]

    when(requestHolder.withHeaders(any[(String, String)])).thenReturn(requestHolder)
    val response = mockResponse
    when(requestHolder.get()(any[ExecutionContext])).thenReturn(Future.successful(response))
    when(requestHolder.post(any[Map[String, Seq[String]]])(any[ExecutionContext])).thenReturn(Future.successful(response))

    requestHolder
  }

  protected def mockResponse: Response = {
    val response = mock[Response]

    when(response.json).thenReturn(JsNull)
    when(response.status).thenReturn(200)
    when(response.cookie(anyString())).thenReturn(None)

    response
  }

  protected def mockCookie: Cookie = {
    val cookie = mock[Cookie]

    when(cookie.value).thenReturn(None)

    cookie
  }

}
