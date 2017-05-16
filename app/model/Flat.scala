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
            val firstSeenAt: Option[Date],
            val lastSeenAt: Option[Date]
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

  override def toString: String = {
    if (price == None && link == None && lastSeenAt == None && firstSeenAt == None) {
      "address: " + address.get + ", " +
        "rooms: " + rooms.get + ", " +
        "size: " + size.get + ", " +
        "floor: " + floor.get
    } else if (lastSeenAt == None && firstSeenAt == None){
      "address: " + address.get + ", " +
        "rooms: " + rooms.get + ", " +
        "size: " + size.get + ", " +
        "floor: " + floor.get + ", " +
        "price: " + price.get + ", " +
        "link: https://www.ss.lv" + link.get
    } else {
      "address: " + address.get + ", " +
        "rooms: " + rooms.get + ", " +
        "size: " + size.get + ", " +
        "floor: " + floor.get + ", " +
        "price: " + price.get + ", " +
        "link: https://www.ss.lv" + link.get + ", " +
        "firstSeenAt: " + firstSeenAt.get + ", " +
        "lastSeenAt: " + lastSeenAt.get
    }
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
      "firstSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(flat.firstSeenAt.get)),
      "lastSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(flat.lastSeenAt.get))
    )
  }


}