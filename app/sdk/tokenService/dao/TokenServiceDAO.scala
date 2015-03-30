package sdk.tokenService.dao

import anorm._
import play.api.db.DB
import play.api.Play
import play.api.Play.current

object TokenServiceDAO {
  def apply(): TokenServiceDAO = {
    Play.current.configuration.getString("heroku.postgresql.hostname") match {
      case None => InMemoryTokenServiceDAO
      case Some(_) => PostgresTokenServiceDAO
    }
  }
}

trait TokenServiceDAO {
  def saveTokenMapping(authToken: String, authCookie: String): Unit

  def findCookie(authToken: String): Option[String]
}

object InMemoryTokenServiceDAO extends TokenServiceDAO {
  var tokenMap = Map[String, String]()

  def saveTokenMapping(authToken: String, authCookie: String): Unit =
    tokenMap += authToken -> authCookie

  def findCookie(authToken: String): Option[String] =
    tokenMap.get(authToken)
}

object PostgresTokenServiceDAO extends TokenServiceDAO {
  def saveTokenMapping(authToken: String, authCookie: String): Unit = {
    DB.withConnection { implicit c =>
      val insert = SQL(
        """
          | INSERT INTO auth_token_cookie (token, cookie)
          | VALUES ({token}, {cookie})
        """.stripMargin).on("token" -> authToken, "cookie" -> authCookie)

      insert.executeUpdate()
    }
  }

  def findCookie(authToken: String): Option[String] = {
    DB.withConnection { implicit c =>
      val select = SQL(
        """
          | SELECT cookie
          | FROM auth_token_cookie
          | WHERE token = {token}
          | LIMIT 1
        """.stripMargin).on("token" -> authToken)

      val result = select().force.headOption.map { row =>
        row[String]("cookie")
      }

      result
    }
  }
}
