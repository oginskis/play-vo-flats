package model.b2c

import model.CommonProps._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Subscription(
                  val subscriptionId: Option[String] = None,
                  val subscriber: String,
                  val priceRange: Option[Range],
                  val sizeRange: Option[Range],
                  val floorRange: Option[Range],
                  val cities: Option[Array[String]],
                  val districts: Option[Array[String]],
                  val actions: Option[Array[String]]
                  ) {

  override def toString(): String = {
    "subscriber: " + subscriber + ", "+
    "priceRange: [ " + priceRange.getOrElse(EmptyProp) +" ], "+
    "sizeRange: [ "+ sizeRange.getOrElse(EmptyProp) +" ], " +
    "floorRange [ "+ floorRange.getOrElse(EmptyProp) +" ], "+
    "cities: " + cities.getOrElse(EmptyProp) + ", "+
    "districts: " + districts.getOrElse(EmptyProp) + ", "+
    "actions: " + actions.getOrElse(EmptyProp)
  }
}

object Subscription {

  implicit val subscriptionWrites: Writes[Subscription] = (
    (JsPath \ "subscriptionId").writeNullable[String] and
      (JsPath \ "subscriber").write[String] and
      (JsPath \ "priceRange").writeNullable[Range] and
      (JsPath \ "sizeRange").writeNullable[Range] and
      (JsPath \ "floorRange").writeNullable[Range] and
      (JsPath \ "cities").writeNullable[Array[String]] and
      (JsPath \ "districts").writeNullable[Array[String]] and
      (JsPath \ "actions").writeNullable[Array[String]]
    )(unlift(Subscription.unapply))

  implicit val subscriptionReads: Reads[Subscription] = (
      (JsPath \ "subscriptionId").readNullable[String] and
      (JsPath \ "subscriber").read[String](email) and
      (JsPath \ "priceRange").readNullable[Range] and
      (JsPath \ "sizeRange").readNullable[Range] and
      (JsPath \ "floorRange").readNullable[Range] and
      (JsPath \ "cities").readNullable[Array[String]] and
      (JsPath \ "districts").readNullable[Array[String]] and
      (JsPath \ "actions").readNullable[Array[String]]
    )(Subscription.apply _)
}
