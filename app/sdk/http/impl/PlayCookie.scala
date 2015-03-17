package sdk.http.impl

import play.api.libs.ws.WSCookie
import sdk.http.Cookie

class PlayCookie(cookie: WSCookie) extends Cookie {
  def value: Option[String] = cookie.value
}
