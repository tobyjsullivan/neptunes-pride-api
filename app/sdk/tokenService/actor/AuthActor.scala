package sdk.tokenService.actor

import akka.actor.{Actor, Props}
import play.api.libs.Codecs
import play.api.cache.Cache
import sdk.tokenService.actor.AuthActor.{GenerateToken, LookupCookie}
import sdk.tokenService.dao.TokenServiceDAO
import sdk.{AuthCookie, AuthToken}
import play.api.Play.current

object AuthActor {
  def props = Props(new AuthActor)

  case class GenerateToken(cookie: AuthCookie)

  case class LookupCookie(token: AuthToken)
}

class AuthActor extends Actor {

  var tokenStore = TokenServiceDAO()

  def receive = {
    case GenerateToken(cookie) => {
      val sToken = Codecs.md5(cookie.value.getBytes)
      val token: AuthToken = AuthToken(sToken)
      tokenStore.saveTokenMapping(sToken, cookie.value)
      Cache.set("cookie_" + sToken, cookie)
      sender ! token
    }

    case LookupCookie(token) => {
      val sToken = token.token

      val oCookie: Option[AuthCookie] = Cache.getAs[AuthCookie]("cookie_" + sToken).orElse {
        val found = tokenStore.findCookie(token.token).map(AuthCookie.apply)

        found.map(cookie => Cache.set("cookie_" + sToken, cookie))

        found
      }

      sender ! oCookie
    }
  }
}


