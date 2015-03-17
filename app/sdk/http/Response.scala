package sdk.http

import play.api.libs.json.JsValue

trait Response {
  def json: JsValue

  def status: Int

  def cookie(key: String): Option[Cookie]
}
