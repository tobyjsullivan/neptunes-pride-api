package sdk.tokenService

import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.concurrent.Akka
import play.api.Play.current
import sdk.{AuthCookie, AuthToken}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object TokenService {
  val authActor = Akka.system.actorOf(AuthActor.props)

  implicit val timeout = Timeout(100 milliseconds)

  def getToken(cookie: AuthCookie)(implicit ec: ExecutionContext): Future[AuthToken] =
    (authActor ? GenerateToken(cookie)).mapTo[AuthToken]

  def lookupCookie(token: AuthToken)(implicit ec: ExecutionContext): Future[AuthCookie] =
    for(
      oCookie <- (authActor ? LookupCookie(token)).mapTo[Option[AuthCookie]];
      cookie <- Future(oCookie.get)
    ) yield cookie
}
