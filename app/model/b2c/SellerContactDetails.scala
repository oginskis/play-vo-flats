package model.b2c

import play.api.libs.json.{Json, Writes}

/**
  * Created by oginskis on 24/06/2017.
  */
class SellerContactDetails(
                    val phoneNumbers: Option[List[String]],
                    val webPage: Option[String],
                    val company: Option[String]
                    ) {

  override def toString = {
    "phoneNumbers: " + (if (phoneNumbers != None && !phoneNumbers.get.isEmpty)
      phoneNumbers.get.mkString(",")
    else SellerContactDetails.EmptyProp) + ", " +
    "webPage: "+ webPage.getOrElse(SellerContactDetails.EmptyProp) + ", " +
    "company: "+ company.getOrElse(SellerContactDetails.EmptyProp)
  }
}

object SellerContactDetails {
  val EmptyProp = "Empty"
  implicit val flatWrites = new Writes[SellerContactDetails] {
    def writes(contactDetails: SellerContactDetails) = {
      if (contactDetails.webPage != None && contactDetails.company != None) {
        Json.obj(
          "phoneNumbers" -> contactDetails.phoneNumbers,
          "webPage" -> contactDetails.webPage,
          "company" -> contactDetails.company
        )
      } else if (contactDetails.webPage == None && contactDetails.company != None) {
        Json.obj(
          "phoneNumbers" -> contactDetails.phoneNumbers,
          "company" -> contactDetails.company
        )
      } else if (contactDetails.webPage != None && contactDetails.company == None) {
        Json.obj(
          "phoneNumbers" -> contactDetails.phoneNumbers,
          "webPage" -> contactDetails.webPage
        )
      } else {
        Json.obj(
          "phoneNumbers" -> contactDetails.phoneNumbers
        )
      }
    }
  }
}
