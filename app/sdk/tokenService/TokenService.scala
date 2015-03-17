package sdk.tokenService

import sdk.{AuthCookie, AuthToken}

import scala.concurrent.{ExecutionContext, Future}

trait TokenService {
  def getToken(oCookie: Option[AuthCookie])(implicit ec: ExecutionContext): Future[AuthToken]

  def lookupCookie(token: AuthToken)(implicit ec: ExecutionContext): Future[AuthCookie]
}
