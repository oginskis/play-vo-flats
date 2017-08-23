package model.b2c

import play.api.libs.json.{JsPath, Reads, Writes}
import model.CommonProps._
import play.api.libs.functional.syntax.unlift
import play.api.libs.functional.syntax._


/**
  * Created by oginskis on 24/06/2017.
  */
case class SellerContactDetails(
                    val phoneNumbers: Option[List[String]],
                    val webPage: Option[String],
                    val company: Option[String]
                    ) {

  override def toString = {
    "phoneNumbers: " + (if (phoneNumbers != None && !phoneNumbers.get.isEmpty)
      phoneNumbers.get.mkString(",")
    else EmptyProp) + ", " +
    "webPage: "+ webPage.getOrElse(EmptyProp) + ", " +
    "company: "+ company.getOrElse(EmptyProp)
  }
}

object SellerContactDetails {

  implicit val sellerContactDetailsWrites: Writes[SellerContactDetails] = (
      (JsPath \ "phoneNumbers").writeNullable[List[String]] and
      (JsPath \ "webPage").writeNullable[String] and
      (JsPath \ "company").writeNullable[String]
    )(unlift(SellerContactDetails.unapply))

  implicit val sellerContactDetailsReads: Reads[SellerContactDetails] = (
      (JsPath \ "phoneNumbers").readNullable[List[String]] and
      (JsPath \ "webPage").readNullable[String] and
      (JsPath \ "company").readNullable[String]
    )(SellerContactDetails.apply _)
}
