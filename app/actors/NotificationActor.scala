package actors

import akka.actor.{Actor, ActorLogging}
import model.CommonProps
import model.b2c.{Flat, FlatNotification}
import play.api.{Configuration, Logger}
import repo.SubscriptionRepo

import scala.services.EmailSendingService
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by oginskis on 12/03/2017.
  */
class NotificationActor (subscriptionRepo: SubscriptionRepo, emailSendingService: EmailSendingService,
                         configuration: Configuration) extends Actor with ActorLogging {

  override def receive: Receive = {
      case flat: Flat => {
        val future = subscriptionRepo.findAllSubscribersForFlat(flat)
        future.map(subscriptions => {
          subscriptions.foreach(
            subscription => {
              val tokenFuture = subscriptionRepo.getSubscriptionToken(subscription.subscriptionId.getOrElse(""))
              tokenFuture.map(result => {
                self ! FlatNotification(flat,subscription,result.getOrElse(CommonProps.EmptyProp))
              })
            }
          )
        })
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
