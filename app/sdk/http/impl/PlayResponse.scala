package sdk.http.impl

import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import sdk.http.{Cookie, Response}

class PlayResponse(resp: WSResponse) extends Response {
  def json: JsValue = resp.json

  def status: Int = resp.status

  def cookie(key: String): Option[Cookie] =
    resp.cookie(key) map { cookie =>
      new PlayCookie(cookie)
    }
}
