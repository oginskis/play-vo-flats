package model.b2c

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._

/**
  * Created by oginskis on 18/05/2017.
  */
case class FlatSearchResult(
                             val address: Option[String],
                             val rooms: Option[String],
                             val size: Option[String],
                             val floor: Option[String],
                             val price: Option[String],
                             val firstSeenAt: Option[Long],
                             val lastSeenAt: Option[Long],
                             val flatId: Option[String]
                           ) {

  val EMPTY_PROP = "Empty"

  override def toString(): String = {
    "address: " + address.getOrElse(EMPTY_PROP) + ", " +
      "rooms: " + rooms.getOrElse(EMPTY_PROP) + ", " +
      "size: " + address.getOrElse(EMPTY_PROP) + ", " +
      "floors: " + floor.getOrElse(EMPTY_PROP) + ", " +
      "price: " + price.getOrElse(EMPTY_PROP) + ", " +
      "firstSeenAt: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(firstSeenAt
      .getOrElse(0l) * 1000)) + ", " +
      "lastSeenAt: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(lastSeenAt
      .getOrElse(0l) * 1000)) + ", " +
      "flatId: " + flatId.getOrElse(EMPTY_PROP)
  }
}

object FlatSearchResult {

  implicit val flatSearchResult = new Reads[FlatSearchResult] {
    override def reads(json: JsValue): JsResult[FlatSearchResult] = {
      val flatSearchResult = FlatSearchResult(
        (json \ "address").asOpt[String],
        (json \ "rooms").asOpt[String],
        (json \ "size").asOpt[String],
        (json \ "floor").asOpt[String],
        (json \ "price").asOpt[String],
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
      "rooms" -> flatSearchResult.rooms,
      "size" -> flatSearchResult.size,
      "floor" -> flatSearchResult.floor,
      "price" -> flatSearchResult.price,
      "firstSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        .format(new Date(flatSearchResult.firstSeenAt.get * 1000))),
      "lastSeenAt" -> (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        .format(new Date(flatSearchResult.lastSeenAt.get * 1000))),
      "flatId" -> flatSearchResult.flatId
    )
  }
}


