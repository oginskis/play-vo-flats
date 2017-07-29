package model.b2c

import model.CommonProps._
import play.api.libs.json._

case class Subscription(
                  val subscriber: Option[String],
                  val priceRange: Option[Range],
                  val sizeRange: Option[Range],
                  val floorRange: Option[Range],
                  val cities: Option[Array[String]],
                  val districts: Option[Array[String]],
                  val actions: Option[Array[String]]
                  ) {

  override def toString(): String = {
    "subscriber: " + subscriber.getOrElse(EmptyProp) + ", "+
    "priceRange: [ " + priceRange.getOrElse(EmptyProp) +" ], "+
    "sizeRange: [ "+ sizeRange.getOrElse(EmptyProp) +" ], " +
    "floorRange [ "+ floorRange.getOrElse(EmptyProp) +" ], "+
    "cities: " + cities.getOrElse(EmptyProp) + ", "+
    "districts: " + districts.getOrElse(EmptyProp) + ", "+
    "actions: " + actions.getOrElse(EmptyProp)
  }
}

object Subscription {

  implicit val subscriptionWrites = new Writes[Subscription] {
    override def writes(subscription: Subscription) = {
      Json.obj(
        "subscriber" -> subscription.subscriber,
        "priceRange" -> subscription.priceRange,
        "sizeRange" -> subscription.sizeRange,
        "floorRange" -> subscription.floorRange,
        "cities" -> subscription.cities,
        "districts" -> subscription.districts,
        "actions" -> subscription.actions
      )
    }
  }
  implicit val subscriptionReads = new Reads[Subscription] {
    override def reads(json: JsValue): JsResult[Subscription] = {
      val subscription = Subscription(
        (json \ "subscriber").asOpt[String],
        (json \ "priceRange").asOpt[Range],
        (json \ "sizeRange").asOpt[Range],
        (json \ "floorRange").asOpt[Range],
        (json \ "cities").asOpt[Array[String]],
        (json \ "districts").asOpt[Array[String]],
        (json \ "actions").asOpt[Array[String]]
      )
      JsSuccess(subscription)
    }
  }
}
