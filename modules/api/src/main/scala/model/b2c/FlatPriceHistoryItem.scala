package model.b2c

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json.{Json, Writes}
import model.CommonProps._

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
  implicit val flatWrites = new Writes[FlatPriceHistoryItem] {
    def writes(flatHistoryItem: FlatPriceHistoryItem) = {
      if (flatHistoryItem.contactDetails == None) {
        Json.obj(
          "link" -> ("https://www.ss.lv" + flatHistoryItem.link.get),
          "price" -> flatHistoryItem.price,
          "firstSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(flatHistoryItem.firstSeenAt.getOrElse(0l)
            * 1000))),
          "lastSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(flatHistoryItem.lastSeenAt.getOrElse(0l)
            * 1000)))
        )
      } else {
        Json.obj(
          "link" -> ("https://www.ss.lv" + flatHistoryItem.link.get),
          "price" -> flatHistoryItem.price,
          "firstSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(flatHistoryItem.firstSeenAt.getOrElse(0l)
            * 1000))),
          "lastSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(flatHistoryItem.lastSeenAt.getOrElse(0l)
            * 1000))),
          "contactDetails" -> flatHistoryItem.contactDetails
        )
      }
    }
  }
}
