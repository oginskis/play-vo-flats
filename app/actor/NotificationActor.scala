package actor

import akka.actor.{ActorLogging, Actor}
import model.Flat
import services.EmailSender

/**
  * Created by oginskis on 12/03/2017.
  */
class NotificationActor (emailSender: EmailSender) extends Actor with ActorLogging {
  override def receive: Receive = {
    case flat: Flat => {
      emailSender.sendEmail(flat)
    }
  }
}
