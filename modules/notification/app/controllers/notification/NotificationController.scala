package controllers.notification

import javax.inject.Inject

import model.b2c.{Error, FlatNotification, SubscriptionActivationRequest, Success}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import scala.services.EmailSendingService

class NotificationController @Inject()(cc: ControllerComponents,
                                       emailSendingService: EmailSendingService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def sendNewFlatNotification = Action(parse.json).async { request => {
    val validationResult = request.body.validate[FlatNotification]
    validationResult.fold(
      errors => {
        Future {
          BadRequest(Json.toJson(Error("Invalid request",
            errors.map(error => {
              ("Invalid value in [" + error._1.toString.tail + "] field")
            }).mkString(", "))))
        }
      },
      notification => {
        val future = emailSendingService.sendFlatNotificationEmail(notification)
        future
          .map(_=>{Ok(Json.toJson(Success("OK","Flat notification email has been sent")))})
          .recover({ case ex: Exception => InternalServerError(Json
            .toJson(Error("Error",s"Error has occurred: ${ex.getMessage}"))) })
      }
    )
    }
  }

  def sendSubscriptionActivationNotification = Action(parse.json).async { request => {
    val validationResult = request.body.validate[SubscriptionActivationRequest]
    validationResult.fold(
      errors => {
        Future {
          BadRequest(Json.toJson(Error("Invalid request",
            errors.map(error => {
              ("Invalid value in [" + error._1.toString.tail + "] field")
            }).mkString(", "))))
        }
      },
      activationRequest => {
        val future = emailSendingService.sendSubscriptionActivationEmail(activationRequest)
        future
          .map(_=>{Ok(Json.toJson(Success("OK","Subscription activation email has been sent")))})
          .recover({ case ex: Exception => InternalServerError(Json
            .toJson(Error("Error",s"Error has occurred: ${ex.getMessage}"))) })
      }
    )
  }
  }

}
