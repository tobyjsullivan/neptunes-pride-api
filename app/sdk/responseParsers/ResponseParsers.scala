package sdk.responseParsers

import play.api.libs.json.{JsError, JsSuccess, JsResult}

import scala.util.{Failure, Success, Try}

trait ResponseParsers {
  protected def tryParse[A](parser: => A): JsResult[A] = {
    Try(parser) match {
      case Success(parsed) => JsSuccess(parsed)
      case Failure(e) => JsError(e.getMessage)
    }
  }
}
