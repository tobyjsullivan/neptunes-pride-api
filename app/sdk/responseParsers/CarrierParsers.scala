package sdk.responseParsers

import play.api.libs.json.{JsResult, JsValue, Reads}
import sdk.model.CarrierOrderAction.CarrierOrderAction
import sdk.model._

object CarrierParsers extends ResponseParsers {
  implicit val carrierParser: Reads[Carrier] = new Reads[Carrier] {
    def reads(jsCarrier: JsValue): JsResult[Carrier] = tryParse {
      Carrier(
        carrierId = (jsCarrier \ "uid").as[Int],
        starId = (jsCarrier \ "ouid").asOpt[Int],
        ships = (jsCarrier \ "st").as[Int],
        name = (jsCarrier \ "n").as[String],
        loopingOrders = (jsCarrier \ "l").as[Int] != 0,
        orders = (jsCarrier \ "o").as[Seq[CarrierOrder]],
        playerId = (jsCarrier \ "puid").as[Int],
        position = Position(
          x = java.lang.Double.parseDouble((jsCarrier \ "x").as[String]),
          y = java.lang.Double.parseDouble((jsCarrier \ "y").as[String])
        ),
        lastPosition = Position(
          x = java.lang.Double.parseDouble((jsCarrier \ "lx").as[String]),
          y = java.lang.Double.parseDouble((jsCarrier \ "ly").as[String])
        )
      )
    }
  }
  
  implicit val carrierOrderParser: Reads[CarrierOrder] = new Reads[CarrierOrder] {
    def reads(jsCarrierOrder: JsValue): JsResult[CarrierOrder] = tryParse {
      val values: Seq[Int] = jsCarrierOrder.as[Seq[Int]]

      CarrierOrder(
        delay = values(0),
        planetId = values(1),
        action = carrierOrderActionMap(values(2)),
        ships = values(3)
      )
    }
  }

  private val carrierOrderActionMap: Map[Int, CarrierOrderAction] = Map(
    0 -> CarrierOrderAction.doNothing,
    1 -> CarrierOrderAction.collectAll,
    2 -> CarrierOrderAction.dropAll,
    3 -> CarrierOrderAction.collect,
    4 -> CarrierOrderAction.drop,
    5 -> CarrierOrderAction.collectAllBut,
    6 -> CarrierOrderAction.dropAllBut,
    7 -> CarrierOrderAction.garrison
  )
}
