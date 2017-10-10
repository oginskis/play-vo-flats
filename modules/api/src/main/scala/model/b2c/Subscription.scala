package model.b2c

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

import model.CommonProps._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Subscription(
                  val subscriptionId: Option[String] = None,
                  val subscriber: String,
                  val priceRange: Option[Range] = None,
                  val sizeRange: Option[Range] = None,
                  val floorRange: Option[Range] = None,
                  val cities: Option[Array[String]] = None,
                  val districts: Option[Array[String]] = None,
                  val actions: Option[Array[String]] = None,
                  val enabled: Option[Boolean] = Option(false),
                  val lastUpdatedDateTime: Option[Long] = Option(Instant.now.getEpochSecond),
                  val language: String = "en"
                  ) {

  override def toString(): String = {
    "subscriber: " + subscriber + ", "+
    "priceRange: [ " + priceRange.getOrElse(EmptyProp) +" ], "+
    "sizeRange: [ "+ sizeRange.getOrElse(EmptyProp) +" ], " +
    "floorRange [ "+ floorRange.getOrElse(EmptyProp) +" ], "+
    "cities: " + cities.getOrElse(EmptyProp) + ", "+
    "districts: " + districts.getOrElse(EmptyProp) + ", "+
    "actions: " + actions.getOrElse(EmptyProp) + ", "+
    "enabled: " + enabled.getOrElse(EmptyProp) + ", "+
    "lastUpdatedDateTime: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
      .format(new Date(lastUpdatedDateTime.getOrElse(0l) * 1000)) + ", "+
    "language: " + language
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
      (JsPath \ "actions").writeNullable[Array[String]] and
      (JsPath \ "enabled").writeNullable[Boolean] and
      (JsPath \ "lastUpdatedDateTime").writeNullable[Long] and
      (JsPath \ "language").write[String]
    )(unlift(Subscription.unapply))

  implicit val subscriptionReads: Reads[Subscription] = (
      (JsPath \ "subscriptionId").readNullable[String] and
      (JsPath \ "subscriber").read[String](email) and
      (JsPath \ "priceRange").readNullable[Range] and
      (JsPath \ "sizeRange").readNullable[Range] and
      (JsPath \ "floorRange").readNullable[Range] and
      (JsPath \ "cities").readNullable[Array[String]] and
      (JsPath \ "districts").readNullable[Array[String]] and
      (JsPath \ "actions").readNullable[Array[String]] and
      (JsPath \ "enabled").readNullable[Boolean] and
      (JsPath \ "lastUpdatedDateTime").readNullable[Long] and
      (JsPath \ "language").read[String]
    )(Subscription.apply _)
}
