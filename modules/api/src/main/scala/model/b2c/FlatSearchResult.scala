package model.b2c

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._
import model.CommonProps._

/**
  * Created by oginskis on 18/05/2017.
  */
case class FlatSearchResult(
                             val address: Option[String],
                             val rooms: Option[Int],
                             val size: Option[Int],
                             val floor: Option[Int],
                             val price: Option[Int],
                             val firstSeenAt: Option[Long],
                             val lastSeenAt: Option[Long],
                             val flatId: Option[String]
                           ) {

  override def toString(): String = {
    "address: " + address.getOrElse(EmptyProp) + ", " +
      "rooms: " + rooms.getOrElse(EmptyProp) + ", " +
      "size: " + address.getOrElse(EmptyProp) + ", " +
      "floors: " + floor.getOrElse(EmptyProp) + ", " +
      "price: " + price.getOrElse(EmptyProp) + ", " +
      "firstSeenAt: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(firstSeenAt
      .getOrElse(0l) * 1000)) + ", " +
      "lastSeenAt: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(lastSeenAt
      .getOrElse(0l) * 1000)) + ", " +
      "flatId: " + flatId.getOrElse(EmptyProp)
  }
}

object FlatSearchResult {

  implicit val flatSearchResult = new Reads[FlatSearchResult] {
    override def reads(json: JsValue): JsResult[FlatSearchResult] = {
      val flatSearchResult = FlatSearchResult(
        (json \ "address").asOpt[String],
        (json \ "numberOfRooms").asOpt[Int],
        (json \ "size").asOpt[Int],
        (json \ "flatFloor").asOpt[Int],
        (json \ "price").asOpt[Int],
        (json \ "firstSeenAtEpoch").asOpt[Long],
        (json \ "lastSeenAtEpoch").asOpt[Long],
        (json \ "id").asOpt[String]
      )
      JsSuccess(flatSearchResult)
    }
  }
  implicit val flatSearchResultWrites = new Writes[FlatSearchResult] {
    def writes(flatSearchResult: FlatSearchResult) = Json.obj(
      "address" -> flatSearchResult.address,
      "numberOfRooms" -> flatSearchResult.rooms,
      "size" -> flatSearchResult.size,
      "flatFloor" -> flatSearchResult.floor,
      "price" -> flatSearchResult.price,
      "firstSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        .format(new Date(flatSearchResult.firstSeenAt.get * 1000))),
      "lastSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        .format(new Date(flatSearchResult.lastSeenAt.get * 1000))),
      "flatId" -> flatSearchResult.flatId
    )
  }
}


