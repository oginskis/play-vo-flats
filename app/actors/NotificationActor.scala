package actors

import actors.helpers.EmailSender
import akka.actor.{Actor, ActorLogging}
import model.b2c.Flat
import play.api.{Configuration, Logger}

/**
  * Created by oginskis on 12/03/2017.
  */
class NotificationActor (configuration: Configuration) extends Actor with ActorLogging {

  val emailSender = new EmailSender(configuration)

  override def receive: Receive = {
      case flat: Flat => {
        try {
          emailSender.sendEmail(flat)
      }
      catch {
        case e: Exception => {
          Logger.error(s"Failed to send an email for $flat: ${e.getMessage}. Will retry...")
          self ! flat
        }
      }
    }
  }
}
