package sdk.responseParsers

import play.api.libs.json._
import sdk.NPClient.UniverseReport
import sdk.model._

import scala.util.{Failure, Success, Try}

object FullUniverseReportParsers extends ResponseParsers {
  import CarrierParsers._

  implicit val universeReportParser: Reads[UniverseReport] = new Reads[UniverseReport] {
    def reads(jsReport: JsValue): JsResult[UniverseReport] = tryParse {
      val game: Game = jsReport.as[Game]
      val players: Seq[Player] = getJsonObjects(jsReport \ "players").map(_.as[Player]).sortBy(_.playerId)
      val stars: Seq[Star] = getJsonObjects(jsReport \ "stars").map(_.as[Star]).sortBy(_.starId)
      val carriers: Seq[Carrier] = getJsonObjects(jsReport \ "fleets").map(_.as[Carrier]).sortBy(_.carrierId)
      UniverseReport(game, players, stars, carriers)
    }
  }

  implicit private val gameParser: Reads[Game] = new Reads[Game] {
    def reads(jsReport: JsValue): JsResult[Game] = tryParse {
      val gameName = (jsReport \ "name").as[String]
      val gameDetails = jsReport.as[GameSettings]
      val gameStatus = jsReport.as[GameStatus]
      val gamePlayer = jsReport.as[GamePlayer]

      Game(
        name = gameName,
        details = Some(gameDetails),
        status = Some(gameStatus),
        player = Some(gamePlayer)
      )
    }
  }

  implicit private val gameSettingsParser: Reads[GameSettings] = new Reads[GameSettings] {
    def reads(jsReport: JsValue): JsResult[GameSettings] = tryParse {
      GameSettings(
        turnBased = (jsReport \ "turn_based").as[Int] != 0,
        turnBasedTimeout = (jsReport \ "turn_based_time_out").as[Int],
        war = (jsReport \ "war").as[Int] != 0,
        tickRate = (jsReport \ "tick_rate").as[Int],
        productionRate = (jsReport \ "production_rate").as[Int],
        totalStars = (jsReport \ "total_stars").as[Int],
        starsForVictory = (jsReport \ "stars_for_victory").as[Int],
        tradeCost = (jsReport \ "trade_cost").as[Int],
        tradeScanned = (jsReport \ "trade_scanned").as[Int] != 0,
        carrierSpeed = (jsReport \ "fleet_speed").as[Double]
      )
    }
  }

  implicit private val gameStatusParser: Reads[GameStatus] = new Reads[GameStatus] {
    def reads(jsReport: JsValue): JsResult[GameStatus] = tryParse {
      GameStatus(
        startTime = (jsReport \ "start_time").as[Long],
        now = (jsReport \ "now").as[Long],
        started = (jsReport \ "started").as[Boolean],
        paused = (jsReport \ "paused").as[Boolean],
        gameOver = (jsReport \ "game_over").as[Int] != 0,
        productions = (jsReport \ "productions").as[Int],
        productionCounter = (jsReport \ "production_counter").as[Int],
        tick = (jsReport \ "tick").as[Int],
        tickFragment = (jsReport \ "tick_fragment").as[Double]
      )
    }
  }

  implicit private val gamePlayerParser: Reads[GamePlayer] = new Reads[GamePlayer] {
    def reads(jsReport: JsValue): JsResult[GamePlayer] = tryParse {
      GamePlayer(
        playerId = (jsReport \ "player_uid").as[Int],
        admin = (jsReport \ "admin").as[Int] > 0
      )
    }
  }

  /**
   * Takes a JSON "map" (an object with integer-named properties) and returns a stream of JsValues which is more useful
   * @param jsonMap The JsValue which contains all the elements in the map.
   * @return A Stream of the values in the same order
   */
  private def getJsonObjects(jsonMap: JsValue): Seq[JsValue] = jsonMap match {
    case jsObj: JsObject => jsObj.value.values.toSeq
    case _ => Seq()
  }

  implicit private val playerParser: Reads[Player] = new Reads[Player] {
    def reads(jsonPlayer: JsValue): JsResult[Player] = tryParse {
      Player(
        playerId = (jsonPlayer \ "uid").as[Int],
        totalEconomy = (jsonPlayer \ "total_economy").as[Int],
        totalIndustry = (jsonPlayer \ "total_industry").as[Int],
        totalScience = (jsonPlayer \ "total_science").as[Int],
        aiControlled = (jsonPlayer \ "ai").as[Int] != 0,
        totalStars = (jsonPlayer \ "total_stars").as[Int],
        totalCarriers = (jsonPlayer \ "total_fleets").as[Int],
        totalShips = (jsonPlayer \ "total_strength").as[Int],
        name = (jsonPlayer \ "alias").as[String],
        scanning = PlayerTechLevel(
          value = (jsonPlayer \ "tech" \ "scanning" \ "value").as[Double],
          level = (jsonPlayer \ "tech" \ "scanning" \ "level").as[Int]
        ),
        hyperspaceRange = PlayerTechLevel(
          value = (jsonPlayer \ "tech" \ "propulsion" \ "value").as[Double],
          level = (jsonPlayer \ "tech" \ "propulsion" \ "level").as[Int]
        ),
        terraforming = PlayerTechLevel(
          value = (jsonPlayer \ "tech" \ "terraforming" \ "value").as[Double],
          level = (jsonPlayer \ "tech" \ "terraforming" \ "level").as[Int]
        ),
        experimentation = PlayerTechLevel(
          value = (jsonPlayer \ "tech" \ "research" \ "value").as[Double],
          level = (jsonPlayer \ "tech" \ "research" \ "level").as[Int]
        ),
        weapons = PlayerTechLevel(
          value = (jsonPlayer \ "tech" \ "weapons" \ "value").as[Double],
          level = (jsonPlayer \ "tech" \ "weapons" \ "level").as[Int]
        ),
        banking = PlayerTechLevel(
          value = (jsonPlayer \ "tech" \ "banking" \ "value").as[Double],
          level = (jsonPlayer \ "tech" \ "banking" \ "level").as[Int]
        ),
        manufacturing = PlayerTechLevel(
          value = (jsonPlayer \ "tech" \ "manufacturing" \ "value").as[Double],
          level = (jsonPlayer \ "tech" \ "manufacturing" \ "level").as[Int]
        ),
        conceded = (jsonPlayer \ "conceded").as[Int] match {
          case 0 => PlayerConcededResult.active
          case 1 => PlayerConcededResult.quit
          case 2 => PlayerConcededResult.awayFromKeyboard
        },
        ready = (jsonPlayer \ "ready").as[Int] != 0,
        missedTurns = (jsonPlayer \ "missed_turns").as[Int],
        renownToGive = (jsonPlayer \ "karma_to_give").as[Int]
      )
    }
  }

  implicit private val starParser: Reads[Star] = new Reads[Star] {
    def reads(jsStar: JsValue): JsResult[Star] = tryParse {
      Star(
        starId = (jsStar \ "uid").as[Int],
        name = (jsStar \ "n").as[String],
        playerId = (jsStar \ "puid").asOpt[Int],
        visible = (jsStar \ "v").as[String] != "0",
        position = Position(
          x = java.lang.Double.parseDouble((jsStar \ "x").as[String]),
          y = java.lang.Double.parseDouble((jsStar \ "y").as[String])
        ),
        economy = (jsStar \ "e").asOpt[Int],
        industry = (jsStar \ "i").asOpt[Int],
        science = (jsStar \ "s").asOpt[Int],
        naturalResources = (jsStar \ "nr").asOpt[Int],
        terraformedResources = (jsStar \ "r").asOpt[Int],
        warpGate = (jsStar \ "ga").asOpt[Int].map(_ != 0),
        ships = (jsStar \ "st").asOpt[Int]
      )
    }
  }
}
