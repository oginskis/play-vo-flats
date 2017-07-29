package model.b2c

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._

import model.CommonProps._


/**
  * Created by oginskis on 30/12/2016.
  */
case class Flat(
                 val status: Flat.Value,
                 val address: Option[String],
                 val rooms: Option[Int],
                 val size: Option[Int],
                 val floor: Option[Int],
                 val maxFloors: Option[Int],
                 val price: Option[Int],
                 val link: Option[String],
                 val firstSeenAt: Option[Long],
                 val lastSeenAt: Option[Long],
                 val city: Option[String],
                 val district: Option[String],
                 val action: Option[String],
                 val expired: Option[String],
                 val flatPriceHistoryItems: Option[List[FlatPriceHistoryItem]],
                 val contactDetails: Option[SellerContactDetails]
               ) {

  def this(status: Flat.Value,
           address: Option[String],
           rooms: Option[Int],
           size: Option[Int],
           floor: Option[Int],
           maxFloors: Option[Int],
           price: Option[Int],
           link: Option[String],
           contactDetails: Option[SellerContactDetails]) = {
    this(status, address, rooms, size, floor, maxFloors, price, link, None, None, None, None, None, None, None, contactDetails)
  }

  def this(status: Flat.Value,
           address: Option[String],
           rooms: Option[Int],
           size: Option[Int],
           floor: Option[Int],
           maxFloors: Option[Int],
           price: Option[Int],
           link: Option[String]) = {
    this(status, address, rooms, size, floor, maxFloors, price, link, None, None, None, None, None, None, None, None)
  }

  def this(address: Option[String],
           rooms: Option[Int],
           size: Option[Int],
           floor: Option[Int],
           maxFloors: Option[Int],
           price: Option[Int],
           link: Option[String],
           city: Option[String],
           district: Option[String],
           action: Option[String],
           contactDetails: Option[SellerContactDetails]) = {
    this(Flat.NA, address, rooms, size, floor, maxFloors, price, link, None, None, city, district, action, None, None, contactDetails)
  }

  def this(address: Option[String],
           rooms: Option[Int],
           size: Option[Int],
           floor: Option[Int],
           maxFloors: Option[Int],
           price: Option[Int],
           link: Option[String],
           city: Option[String],
           district: Option[String],
           action: Option[String]) = {
    this(Flat.NA, address, rooms, size, floor, maxFloors, price, link, None, None, city, district, action, None, None, None)
  }


  def this(address: Option[String],
           rooms: Option[Int],
           size: Option[Int],
           floor: Option[Int],
           maxFloors: Option[Int],
           city: Option[String],
           district: Option[String],
           action: Option[String]) = {
    this(Flat.NA, address, rooms, size, floor, maxFloors, None, None, None, None, city, district, action, None, None, None)
  }

  def this(expired: Option[String]) ={
    this(Flat.NA,None,None,None,None,None,None,None,None,None,None,None,None,expired,None,None)
  }

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