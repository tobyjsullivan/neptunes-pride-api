package sdk.http

import scala.concurrent.{ExecutionContext, Future}

trait RequestHolder {
  def withHeaders(headers: (String, String)*): RequestHolder

  def post(body: Map[String, Seq[String]])(implicit  ec: ExecutionContext): Future[Response]

  def get()(implicit  ec: ExecutionContext): Future[Response]
}
