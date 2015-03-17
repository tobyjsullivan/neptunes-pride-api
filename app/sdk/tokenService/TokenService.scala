package sdk.tokenService

import sdk.{AuthToken, AuthCookie}

import scala.concurrent.{Future, ExecutionContext}

trait TokenService {
  def getToken(oCookie: Option[AuthCookie])(implicit ec: ExecutionContext): Future[AuthToken]

  def lookupCookie(token: AuthToken)(implicit ec: ExecutionContext): Future[AuthCookie]
}
