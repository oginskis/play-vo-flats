package controllers.subscription

import javax.inject.Inject

import model.CommonProps
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import repo.SubscriptionRepo
import model.b2c.Error

class SubscriptionController @Inject()(cc:ControllerComponents,subscriptionRepo: SubscriptionRepo)
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
    if (email.matches(CommonProps.EmailRegexp)){
      Ok(Json.toJson(subscriptionRepo.findAllSubscriptionsForEmail(email)))
    } else {
      BadRequest(Json.toJson(new Error("Invalid email","Invalid email format")))
    }
  }

}
