package sdk.http.impl

import play.api.libs.ws.WS
import play.api.Play.current
import sdk.http.{RequestHolder, WebService}

object PlayWebService extends WebService {
  def url(url: String): RequestHolder = new PlayRequestHolder(WS.url(url))
}
