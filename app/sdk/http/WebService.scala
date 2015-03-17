package sdk.http

trait WebService {
  def url(url: String): RequestHolder
}
