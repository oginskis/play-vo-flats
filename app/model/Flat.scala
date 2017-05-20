package model

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._


/**
  * Created by oginskis on 30/12/2016.
  */
case class Flat(
            val address: Option[String],
            val rooms: Option[String],
            val size: Option[Int],
            val floor: Option[String],
            val price: Option[Int],
            val link: Option[String],
            val firstSeenAt: Option[Long],
            val lastSeenAt: Option[Long]
          ) {

  def this(address: Option[String],
           rooms: Option[String],
           size: Option[Int],
           floor: Option[String]) = {
    this(address,rooms,size,floor,None,None,None,None)
  }

  def this(address: Option[String],
           rooms: Option[String],
           size: Option[Int],
           floor: Option[String],
           price: Option[Int],
           link: Option[String]) = {
    this(address,rooms,size,floor,price,link,None,None)
  }

  val EMPTY_PROP = "Empty"

  override def toString: String = {
      "address: " + address.getOrElse(EMPTY_PROP) + ", " +
      "rooms: " + rooms.getOrElse(EMPTY_PROP) + ", " +
      "size: " + size.getOrElse(EMPTY_PROP) + ", " +
      "floor: " + floor.getOrElse(EMPTY_PROP) + ", " +
      "price: " + price.getOrElse(EMPTY_PROP) + ", " +
      "link: https://www.ss.lv" + link.get + ", " +
      "firstSeenAt: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(firstSeenAt.getOrElse(0l)
        * 1000)) + ", " +
      "lastSeenAt: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(lastSeenAt.getOrElse(0l)
        * 1000))
    }
}

  object Flat extends Enumeration{
    val Added,Updated = Value
    implicit val flatWrites = new Writes[Flat] {
      def writes(flat: Flat) = Json.obj(
        "address" -> flat.address,
        "rooms" -> flat.rooms,
        "size" -> flat.size,
        "floor" -> flat.floor,
        "price" -> flat.price,
        "link" -> ("https://www.ss.lv"+flat.link.get),
        "firstSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(flat.firstSeenAt.getOrElse(0l)
          * 1000))),
        "lastSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(flat.lastSeenAt.getOrElse(0l)
          * 1000)))
      )
    }
}