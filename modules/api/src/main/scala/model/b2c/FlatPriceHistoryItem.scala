package model.b2c

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json.{JsPath, Reads, Writes}
import model.CommonProps._
import play.api.libs.functional.syntax.unlift
import play.api.libs.functional.syntax._

/**
  * Created by oginskis on 21/05/2017.
  */
case class FlatPriceHistoryItem(
                            val link: Option[String],
                            val price: Option[Int],
                            val firstSeenAt: Option[Long],
                            val lastSeenAt: Option[Long],
                            val contactDetails: Option[SellerContactDetails]
                          ) {

  override def toString: String = {
      "link: " + price.getOrElse(EmptyProp) + ", " +
      "price: " + price.getOrElse(EmptyProp) + ", " +
      "firstSeenAt: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(firstSeenAt.getOrElse(0l)
      * 1000)) + ", " +
      "lastSeenAt: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(lastSeenAt.getOrElse(0l)
      * 1000)) + ", " +
      "contactDetails: " + contactDetails.getOrElse(EmptyProp)
  }

}

object FlatPriceHistoryItem {

  implicit val flatPriceHistoryItemWrites: Writes[FlatPriceHistoryItem] = (
      (JsPath \ "link").writeNullable[String] and
      (JsPath \ "price").writeNullable[Int] and
      (JsPath \ "firstSeenAt").writeNullable[Long] and
      (JsPath \ "lastSeenAt").writeNullable[Long] and
      (JsPath \ "sellerContactDetails").writeNullable[SellerContactDetails]
    )(unlift(FlatPriceHistoryItem.unapply))

  implicit val flatPriceHistoryItemReads: Reads[FlatPriceHistoryItem] = (
      (JsPath \ "link").readNullable[String] and
      (JsPath \ "price").readNullable[Int] and
      (JsPath \ "firstSeenAt").readNullable[Long] and
      (JsPath \ "lastSeenAt").readNullable[Long] and
      (JsPath \ "sellerContactDetails").readNullable[SellerContactDetails]
    )(FlatPriceHistoryItem.apply _)
}
