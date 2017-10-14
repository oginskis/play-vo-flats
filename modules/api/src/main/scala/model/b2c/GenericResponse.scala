package model.b2c

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Writes}

import play.api.libs.functional.syntax._

case class GenericResponse(properties: List[Item]) {

  override def toString:String = {
    def append(remainingItems: List[Item], propertyString: String) : String = {
      if (remainingItems.size > 0) {
        val currentPropertyString = s"${if (propertyString.size > 0) s"$propertyString; " else s""}key: " +
          s"${remainingItems.head.key}, value: ${remainingItems.head.value}"
        append(remainingItems.tail, currentPropertyString)
      } else {
        propertyString
      }
    }
    append(properties, "")
  }

  def append(item: Item): GenericResponse = {
    GenericResponse(item :: properties)
  }
}

case class Item (key: String,value: String)

object GenericResponse {

  implicit val writesItems: Writes[Item] = (
    (JsPath \ "key").write[String] and
    (JsPath \ "value").write[String]
  )(unlift(Item.unapply))

  implicit val writesGenericResponse: Writes[GenericResponse] =
    (JsPath \ "properties").write[List[Item]].contramap(_.properties)
}







