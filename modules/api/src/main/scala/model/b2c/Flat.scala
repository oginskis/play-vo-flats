package model.b2c

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._

import model.CommonProps._


/**
  * Created by oginskis on 30/12/2016.
  */
case class Flat(
                 val status: Flat.Value = Flat.New,
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
  val New,SeenBefore,NA = Value

  implicit val flatWrites = new Writes[Flat] {
    def writes(flat: Flat) = {
      if (flat.contactDetails == None) {
        Json.obj(
          "address" -> flat.address,
          "rooms" -> flat.rooms,
          "size" -> flat.size,
          "floor" -> flat.floor,
          "maxFloors" -> flat.maxFloors,
          "price" -> flat.price,
          "link" -> ("https://www.ss.lv" + flat.link.get),
          "city" -> flat.city,
          "district" -> flat.district,
          "action" -> flat.action,
          "firstSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(flat.firstSeenAt.getOrElse(0l)
            * 1000))),
          "lastSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(flat.lastSeenAt.getOrElse(0l)
            * 1000))),
          "expired" -> flat.expired,
          "flatPriceHistoryItems" -> flat.flatPriceHistoryItems)
      } else {
        Json.obj(
          "address" -> flat.address,
          "rooms" -> flat.rooms,
          "size" -> flat.size,
          "floor" -> flat.floor,
          "maxFloors" -> flat.maxFloors,
          "price" -> flat.price,
          "link" -> ("https://www.ss.lv" + flat.link.get),
          "city" -> flat.city,
          "district" -> flat.district,
          "action" -> flat.action,
          "firstSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(flat.firstSeenAt.getOrElse(0l)
            * 1000))),
          "lastSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(flat.lastSeenAt.getOrElse(0l)
            * 1000))),
          "expired" -> flat.expired,
          "contactDetails" -> flat.contactDetails,
          "flatPriceHistoryItems" -> flat.flatPriceHistoryItems)
      }

    }
  }

}