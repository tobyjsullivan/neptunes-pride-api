package sdk.tokenService

import akka.actor.{Actor, Props}
import play.api.libs.Codecs
import sdk.{AuthCookie, AuthToken}

case class GenerateToken(cookie: AuthCookie)
case class LookupCookie(token: AuthToken)

object AuthActor {
  def props = Props(new AuthActor)
}

class AuthActor extends Actor {
  var tokenMap = Map[AuthToken, AuthCookie]()

  def receive = {
    case GenerateToken(cookie) =>
      val token = AuthToken(Codecs.md5(cookie.value.getBytes))

      tokenMap += token -> cookie

      sender ! token
    case LookupCookie(token) =>
      sender ! tokenMap.get(token)
  }
}


