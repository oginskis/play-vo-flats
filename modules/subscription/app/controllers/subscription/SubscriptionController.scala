package controllers.subscription

import javax.inject.Inject

import model.CommonProps
import model.b2c.{Error, Flat, Subscription}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import repo.SubscriptionRepo

class SubscriptionController @Inject()(cc: ControllerComponents, subscriptionRepo: SubscriptionRepo)
  extends AbstractController(cc) {

  def getSubscriptionById(id: String) = Action {
    if (id.size == 24 && id.matches(CommonProps.HexadecimalRegexp)) {
      val subscription = subscriptionRepo.getSubscriptionById(id)
      if (subscription == None) {
        NotFound(CommonProps.EmptyResponse)
      } else {
        Ok(Json.toJson(subscriptionRepo.getSubscriptionById(id)))
      }
    }
    else BadRequest(Json.toJson(Error.mustBeHexadecimal("SubscriptionId")))
  }

  def deleteSubscriptionById(id: String) = Action {
    if (id.size == 24 && id.matches(CommonProps.HexadecimalRegexp)) {
      if (subscriptionRepo.deleteSubscriptionById(id) > 0) {
        Ok(CommonProps.EmptyResponse)
      }
      else NotFound(CommonProps.EmptyResponse)
    }
    else BadRequest(Json.toJson(Error.mustBeHexadecimal("SubscriptionId")))
  }

  def getAllSubscriptionsForEmail(email: String) = Action {
    if (email.matches(CommonProps.EmailRegexp)) {
      val subscriptions = subscriptionRepo.findAllSubscriptionsForEmail(email)
      if (subscriptions.size > 0) {
        Ok(Json.toJson(subscriptions))
      } else {
        NotFound(CommonProps.EmptyResponse)
      }
    } else {
      BadRequest(Json.toJson(new Error("Invalid email", "Invalid email format")))
    }
  }

  def createSubscription() = Action(parse.json) { request => {
    val result = request.body.validate[Subscription]
    result.fold(
      errors => {
        BadRequest(Json.toJson(new Error("Invalid request",
          errors.map(error => {
            ("Invalid value in [" + error._1.toString.tail + "] field")
          }).mkString(", ")
        )))
      },
      subscription => {
        subscriptionRepo.createSubscription(subscription)
        Created(CommonProps.EmptyResponse)
      }
    )
  }

  }

}
