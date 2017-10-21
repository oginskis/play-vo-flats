package controllers.subscription

import javax.inject.Inject

import model.CommonProps
import model.b2c.{Error, Flat, GenericResponse, Item, Subscription, Success}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import repo.SubscriptionRepo
import CommonProps._

import scala.concurrent.{ExecutionContext, Future}
import scala.services.EmailSendingService

class SubscriptionController @Inject()(cc: ControllerComponents, subscriptionRepo: SubscriptionRepo,
                                       emailSendingService: EmailSendingService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getSubscriptionById(subscriptionId: String) = Action.async {
    subscriptionId match {
      case id if id.matches(HexadecimalRegexp) => {
        val future = subscriptionRepo.getSubscriptionById(id)
        future.map(result => {
          result match {
            case Some(value) => Ok(Json.toJson(value))
            case None => NotFound(EmptyResponse)
          }
        })
      }
      case _ => Future {BadRequest(Json.toJson(Error.mustBeHexadecimal("SubscriptionId")))}
    }
  }

  def enableSubscription(token: String) = Action.async {
    token match {
      case activationToken if activationToken.matches(HexadecimalRegexp32) => {
        val future = subscriptionRepo.enableSubscription(activationToken)
        future.map(result => {
          if (result) {
            Ok(Json.toJson(Success("OK","Subscription has been activated")))
          } else {
            NotFound(EmptyResponse)
          }
          Ok("")
        })
      }
      case _ => Future {BadRequest(Json.toJson(Error.mustBeHexadecimal("activationToken")))}
    }
  }

  def disableSubscription(token: String) = Action.async {
    token match {
      case activationToken if activationToken.matches(HexadecimalRegexp32) => {
        val future = subscriptionRepo.disableSubscription(activationToken)
        future.map(result => {
          if (result) {
            Ok(Json.toJson(Success("OK","Subscription has been disabled")))
          } else {
            NotFound(EmptyResponse)
          }
        })
      }
      case _ => Future{BadRequest(Json.toJson(Error.mustBeHexadecimal("activationToken")))}
    }
  }

  def getSubscriptionToken(subscriptionId: String) = Action.async {
    subscriptionId match {
      case id if id.matches(HexadecimalRegexp) => {
        val future = subscriptionRepo.getSubscriptionToken(subscriptionId)
        future.map({result => {
          result match {
            case Some(value) => Ok(Json.toJson(GenericResponse(List(Item("token",value)))))
            case None => NotFound(EmptyResponse)
          }
        }
        })
      }
      case _ => Future{BadRequest(Json.toJson(Error.mustBeHexadecimal("SubscriptionId")))}
    }
  }

  def deleteSubscriptionById(subscriptionId: String) = Action.async {
    subscriptionId match {
      case id if id.matches(HexadecimalRegexp) => {
        val future = subscriptionRepo.deleteSubscriptionById(subscriptionId)
        future.map({result => {
          if (result>0){
            Ok(CommonProps.EmptyResponse)
          } else NotFound(EmptyResponse)
        }
        })
      }
      case _ => Future{BadRequest(Json.toJson(Error.mustBeHexadecimal("SubscriptionId")))}
    }
  }

  def getAllSubscriptionsForEmail(email: String) = Action.async {
    email match {
      case email if email.matches(EmailRegexp) => {
        val future = subscriptionRepo.findAllSubscriptionsForEmail(email)
        future.map({result => {
          if (result.size > 0) {
            Ok(Json.toJson(result))
          } else {
            NotFound(EmptyResponse)
          }
        }
        })
      }
      case _ => Future{BadRequest(Json.toJson(Error("Invalid email", "Invalid email format")))}
    }
  }

  def getAllWhoSubscribedFor() = Action(parse.json).async { request => {
    val validationResult = request.body.validate[Flat]
    validationResult.fold(
      errors => {
        Future{BadRequest(Json.toJson(Error("Invalid request",
          errors.map(error => {
            ("Invalid value in [" + error._1.toString.tail + "] field")
          }).mkString(", ")
        )))}
      },
      flat => {
        val future = subscriptionRepo.findAllSubscribersForFlat(flat)
        future.map(result => {
          Ok(Json.toJson(result))
        })
      }
    )
  }
  }

  def createSubscription() = Action(parse.json).async { request => {
    val validationResult = request.body.validate[Subscription]
    validationResult.fold(
      errors => {
        Future {
          BadRequest(Json.toJson(Error("Invalid request",
            errors.map(error => {
              ("Invalid value in [" + error._1.toString.tail + "] field")
            }).mkString(", "))))
        }
      },
      subscription => {
        val future = subscriptionRepo.createSubscription(subscription)
        future.map(result => {
          if (result) {
            Created(Json.toJson(Success("OK", "Subscription has been created")))
          } else InternalServerError(EmptyResponse)
        }
        )
      }
    )
  }
  }

}
