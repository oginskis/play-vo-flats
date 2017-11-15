package model.b2c

import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class FlatNotification(
                           val flat: Flat,
                           val subscription: Subscription,
                           val token: String
                           ) {

    override def toString: String = {
      "Flat: " + flat.toString + ", "+
      "Subscription: " + flat.toString
    }
}

object FlatNotification {

  implicit val flatNotificationWrites: Writes[FlatNotification] = (
    (JsPath \ "flat").write[Flat] and
    (JsPath \ "subscription").write[Subscription] and
    (JsPath \ "token").write[String]
    )(unlift(FlatNotification.unapply))

  implicit val flatNotificationReads: Reads[FlatNotification] = (
    (JsPath \ "flat").read[Flat] and
    (JsPath \ "subscription").read[Subscription] and
    (JsPath \ "token").read[String]
    )(FlatNotification.apply _)
}
