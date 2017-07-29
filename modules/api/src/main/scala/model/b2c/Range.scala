package model.b2c

import play.api.libs.json._
import model.CommonProps._

case class Range(
                from:Option[Int],
                to:Option[Int]
                ) {

  override def toString(): String = {
    "from: " + from.getOrElse(EmptyProp) + ", "+
  "to: " + to.getOrElse(EmptyProp)
  }
}

object Range {

  implicit val rangeWrites = new Writes[Range] {
    override def writes(range: Range)= {
      Json.obj(
        "from" -> range.from,
        "to" -> range.to
      )
    }
  }
  implicit val rangeReads = new Reads[Range] {
    override def reads(json: JsValue): JsResult[Range] = {
      val range = Range(
        (json \ "from").asOpt[Int],
        (json \ "to").asOpt[Int]
      )
      JsSuccess(range)
    }
  }
}
