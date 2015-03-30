package sdk.model

import play.api.data.validation.ValidationError
import play.api.libs.json._
import sdk.model.CarrierOrderAction.CarrierOrderAction

import scala.util.{Failure, Success, Try}

object Carrier {
  implicit val fmt: Format[Carrier] = Json.format[Carrier]
}

case class Carrier(
  carrierId: Int,
  starId: Option[Int],
  ships: Int,
  name: String,
  loopingOrders: Boolean,
  orders: Seq[CarrierOrder],
  playerId: Int,
  position: Position,
  lastPosition: Position
)

object CarrierOrder {
  implicit val fmt: Format[CarrierOrder] = Json.format[CarrierOrder]
}

case class CarrierOrder(
  delay: Int,
  starId: Int,
  action: CarrierOrderAction,
  ships: Int
)

object CarrierOrderAction extends Enumeration {
  type CarrierOrderAction = Value
  val DoNothing, CollectAll, Collect, CollectAllBut, DropAll, Drop, DropAllBut, Garrison = Value

  implicit val fmt: Format[CarrierOrderAction] = new Format[CarrierOrderAction] {
    def reads(json: JsValue): JsResult[CarrierOrderAction] = Try(CarrierOrderAction.withName(json.as[String])) match {
      case Success(result) => JsSuccess(result)
      case Failure(e) => JsError(ValidationError(e.getMessage))
    }

    def writes(o: CarrierOrderAction): JsValue = JsString(o.toString)
  }
}