package model.b2c

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._
import model.CommonProps._
import play.api.libs.functional.syntax.unlift
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

/**
  * Created by oginskis on 30/12/2016.
  */
case class Flat(
                 val status: String = "NA",
                 val address: Option[String] = None,
                 val rooms: Option[Int] = None,
                 val size: Option[Int] = None,
                 val floor: Option[Int] = None,
                 val maxFloors: Option[Int] = None,
                 val price: Option[Int] = None,
                 val link: Option[String] = None,
                 val firstSeenAt: Option[Long] = None,
                 val lastSeenAt: Option[Long] = None,
                 val city: Option[String] = None,
                 val district: Option[String] = None,
                 val action: Option[String] = None,
                 val expired: Option[String] = None,
                 val flatPriceHistoryItems: Option[List[FlatPriceHistoryItem]] = None,
                 val contactDetails: Option[SellerContactDetails] = None
               ) {

  override def toString: String = {
      "address: " + address.getOrElse(EmptyProp) + ", " +
      "rooms: " + rooms.getOrElse(EmptyProp) + ", " +
      "size: " + size.getOrElse(EmptyProp) + ", " +
      "floor: " + floor.getOrElse(EmptyProp) + ", " +
      "maxFloors: " + maxFloors.getOrElse(EmptyProp) + ", "+
      "price: " + price.getOrElse(EmptyProp) + ", " +
      "link: https://www.ss.lv" + link.getOrElse(EmptyProp) + ", " +
      "city: " + city.getOrElse(EmptyProp) + ", "+
      "district: "+ district.getOrElse(EmptyProp) + ", "+
      "action: "+ action.getOrElse(EmptyProp) + ", "+
      "firstSeenAt: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(firstSeenAt.getOrElse(0l)
      * 1000)) + ", " +
      "lastSeenAt: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(lastSeenAt.getOrElse(0l)
      * 1000)) + ", " +
      "expired: "+expired.getOrElse(EmptyProp) + ", " +
      "contactDetails: " + contactDetails.getOrElse(EmptyProp)
  }
}

object Flat extends Enumeration {

  implicit val flatWrites: Writes[Flat] = (
      (JsPath \ "status").write[String] and
      (JsPath \ "address").writeNullable[String] and
      (JsPath \ "rooms").writeNullable[Int] and
      (JsPath \ "size").writeNullable[Int] and
      (JsPath \ "floor").writeNullable[Int] and
      (JsPath \ "maxFloors").writeNullable[Int] and
      (JsPath \ "price").writeNullable[Int] and
      (JsPath \ "link").writeNullable[String] and
      (JsPath \ "firstSeenAt").writeNullable[Long] and
      (JsPath \ "lastSeenAt").writeNullable[Long] and
      (JsPath \ "city").writeNullable[String] and
      (JsPath \ "district").writeNullable[String] and
      (JsPath \ "action").writeNullable[String] and
      (JsPath \ "expired").writeNullable[String] and
      (JsPath \ "flatPriceHistoryItems").writeNullable[List[FlatPriceHistoryItem]] and
      (JsPath \ "sellerContactDetails").writeNullable[SellerContactDetails]
    )(unlift(Flat.unapply))

  implicit val flatReads: Reads[Flat] = (
      (JsPath \ "status").read[String] and
      (JsPath \ "address").readNullable[String](minLength(3)) and
      (JsPath \ "rooms").readNullable[Int](min(-1)) and
      (JsPath \ "size").readNullable[Int](min(10)) and
      (JsPath \ "floor").readNullable[Int](min(-1)) and
      (JsPath \ "maxFloors").readNullable[Int](min(0)) and
      (JsPath \ "price").readNullable[Int](min(0)) and
      (JsPath \ "link").readNullable[String] and
      (JsPath \ "firstSeenAt").readNullable[Long] and
      (JsPath \ "lastSeenAt").readNullable[Long] and
      (JsPath \ "city").readNullable[String] and
      (JsPath \ "district").readNullable[String] and
      (JsPath \ "action").readNullable[String] and
      (JsPath \ "expired").readNullable[String] and
      (JsPath \ "flatPriceHistoryItems").readNullable[List[FlatPriceHistoryItem]] and
      (JsPath \ "sellerContactDetails").readNullable[SellerContactDetails]
    )(Flat.apply _)
}