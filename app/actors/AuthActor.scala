package actors

import akka.actor.{Props, Actor}
import play.api.libs.Codecs

case class AuthCookie(value: String) extends AnyVal
case class AuthToken(token: String) extends AnyVal
case class GenerateToken(cookie: AuthCookie)
case class LookupAuthCookie(token: AuthToken)

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
    case LookupAuthCookie(token) =>
      sender ! tokenMap.get(token)
  }
}


