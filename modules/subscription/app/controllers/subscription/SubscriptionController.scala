package controllers.subscription

import javax.inject.Inject

import model.CommonProps
import model.b2c.{Error, Flat, Subscription, Success}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import repo.SubscriptionRepo
import CommonProps._

import scala.services.EmailSendingService

class SubscriptionController @Inject()(cc: ControllerComponents, subscriptionRepo: SubscriptionRepo,
                                       emailSendingService: EmailSendingService)
  extends AbstractController(cc) {

  def getSubscriptionById(subscriptionId: String) = Action {
    subscriptionId match {
      case id if id.matches(HexadecimalRegexp) => {
        val subscription = subscriptionRepo.getSubscriptionById(id)
        if (subscription == None) {
          NotFound(EmptyResponse)
        } else {
          Ok(Json.toJson(subscriptionRepo.getSubscriptionById(id)))
        }
      }
      case _ => BadRequest(Json.toJson(Error.mustBeHexadecimal("SubscriptionId")))
    }
  }

  def enableSubscription(token: String) = Action {
    token match {
      case activationToken if activationToken.matches(HexadecimalRegexp32) => {
        val result = subscriptionRepo.enableSubscription(activationToken)
        if (result) {
            Ok(Json.toJson(Success("OK","Subscription has been activated")))
        } else {
            NotFound(EmptyResponse)
        }
      }
      case _ => BadRequest(Json.toJson(Error.mustBeHexadecimal("activationToken")))
    }
  }

  def deleteSubscriptionById(subscriptionId: String) = Action {
    subscriptionId match {
      case id if id.matches(HexadecimalRegexp) => {
        if (subscriptionRepo.deleteSubscriptionById(subscriptionId) > 0) {
          Ok(CommonProps.EmptyResponse)
        }
        else NotFound(EmptyResponse)
      }
      case _ => BadRequest(Json.toJson(Error.mustBeHexadecimal("SubscriptionId")))
    }
  }

  def getAllSubscriptionsForEmail(email: String) = Action {
    email match {
      case email if email.matches(EmailRegexp) => {
        val subscriptions = subscriptionRepo.findAllSubscriptionsForEmail(email)
        if (subscriptions.size > 0) {
          Ok(Json.toJson(subscriptions))
        } else {
          NotFound(EmptyResponse)
        }
      }
      case _ => BadRequest(Json.toJson(Error("Invalid email", "Invalid email format")))
    }
  }

  def getAllWhoSubscribedFor() = Action(parse.json) { request => {
    val result = request.body.validate[Flat]
    result.fold(
      errors => {
        BadRequest(Json.toJson(Error("Invalid request",
          errors.map(error => {
            ("Invalid value in [" + error._1.toString.tail + "] field")
          }).mkString(", ")
        )))
      },
      flat => {
        if (flat.price==None || flat.size==None || flat.floor==None || flat.city==None ||
          flat.district==None || flat.action==None){
          BadRequest(Json.toJson(Error("Invalid request","city,district,action,floor,size and price properties" +
            " must be present on Flat entity")))
        } else {
          val subscribers = subscriptionRepo.findAllSubscribersForFlat(flat)
          Ok(Json.toJson(subscribers))
        }
      }
    )
    }
  }

  def createSubscription() = Action(parse.json) { request => {
    val result = request.body.validate[Subscription]
    result.fold(
      errors => {
        BadRequest(Json.toJson(Error("Invalid request",
          errors.map(error => {
            ("Invalid value in [" + error._1.toString.tail + "] field")
          }).mkString(", ")
        )))
      },
      subscription => {
        subscriptionRepo.createSubscription(subscription)
        Created(Json.toJson(Success("OK","Subscription has been created")))
      }
    )
    }
  }

}
