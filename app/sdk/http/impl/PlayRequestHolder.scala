package sdk.http.impl

import play.api.libs.ws._
import sdk.http.{RequestHolder, Response}

import scala.concurrent.{ExecutionContext, Future}

class PlayRequestHolder(holder: WSRequestHolder) extends RequestHolder {
  def withHeaders(headers: (String, String)*): RequestHolder =
    new PlayRequestHolder(holder.withHeaders(headers: _*))

  def get()(implicit  ec: ExecutionContext): Future[Response] =
    holder.get() map { resp =>
      new PlayResponse(resp)
    }

  def post(body: Map[String, Seq[String]])(implicit ec: ExecutionContext): Future[Response] =
    holder.post(body) map { resp =>
      new PlayResponse(resp)
    }
}
