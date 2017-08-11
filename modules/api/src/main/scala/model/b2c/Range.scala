package model.b2c

import model.CommonProps._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

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

  implicit val rangeWrites: Writes[Range] = (
    (JsPath \ "from").writeNullable[Int] and (JsPath \ "to").writeNullable[Int]
    )(unlift(Range.unapply))

  implicit val rangeReads: Reads[Range] = (
    (JsPath \ "from").readNullable[Int](min(1)) and (JsPath \ "to").readNullable[Int]
    )(Range.apply _)
}
