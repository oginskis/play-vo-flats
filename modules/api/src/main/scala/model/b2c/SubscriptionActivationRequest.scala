package model.b2c

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._
import java.lang.String._

case class SubscriptionActivationRequest(
                                        activationToken: String,
                                        subscription: Subscription
                                        ) {

  override def toString = {
    format(s"activationToken: $activationToken, subscription: $subscription")
  }
}

object SubscriptionActivationRequest {

  implicit val activationRequestWrites: Writes[SubscriptionActivationRequest] = (
    (JsPath \ "activationToken").write[String] and
    (JsPath \ "subscription").write[Subscription]
    )(unlift(SubscriptionActivationRequest.unapply))

  implicit val activationRequestReads: Reads[SubscriptionActivationRequest] = (
    (JsPath \ "activationToken").read[String] and
    (JsPath \ "subscriber").read[Subscription]
    )(SubscriptionActivationRequest.apply _)
}
