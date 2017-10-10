package model.b2c

import model.CommonProps._
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class FlatNotification(
                           val flat: Option[Flat],
                           val subscription: Option[Subscription]
                           ) {

    override def toString: String = {
      "Flat: " + flat.getOrElse(EmptyProp) + ", "+
      "Subscription: " + flat.getOrElse(EmptyProp)
    }
}

object FlatNotification {

  implicit val flatNotificationWrites: Writes[FlatNotification] = (
    (JsPath \ "flat").writeNullable[Flat] and
    (JsPath \ "subscription").writeNullable[Subscription]
    )(unlift(FlatNotification.unapply))

  implicit val flatNotificationReads: Reads[FlatNotification] = (
    (JsPath \ "flat").readNullable[Flat] and
    (JsPath \ "subscription").readNullable[Subscription]
    )(FlatNotification.apply _)
}
