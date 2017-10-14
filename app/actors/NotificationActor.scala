package actors

import actors.helpers.EmailSender
import akka.actor.{Actor, ActorLogging}
import model.b2c.{Flat, FlatNotification}
import play.api.{Configuration, Logger}
import repo.SubscriptionRepo

import scala.services.EmailSendingService
import scala.util.Try

/**
  * Created by oginskis on 12/03/2017.
  */
class NotificationActor (subscriptionRepo: SubscriptionRepo, emailSendingService: EmailSendingService,
                         configuration: Configuration) extends Actor with ActorLogging {

  val emailSender = new EmailSender(configuration)

  override def receive: Receive = {
      case flat: Flat => {
        val subscriptions = subscriptionRepo.findAllSubscribersForFlat(flat)
        subscriptions.foreach(
          subscription => {
            self ! FlatNotification(Option(flat),Option(subscription),subscriptionRepo
              .getSubscriptionToken(subscription.subscriptionId.getOrElse("")))
          }
        )
      }
      case flatNotification: FlatNotification => {
        Try(emailSendingService.sendFlatNotificationEmail(flatNotification))
          .getOrElse(
            {
              Logger.error(s"Failed to send an email for $flatNotification. Will retry...")
              self ! flatNotification
            }
          )
      }
  }
}
