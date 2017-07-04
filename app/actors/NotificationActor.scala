package actors

import actors.helpers.EmailSender
import akka.actor.{Actor, ActorLogging}
import model.b2c.Flat
import play.api.Configuration

/**
  * Created by oginskis on 12/03/2017.
  */
class NotificationActor (configuration: Configuration) extends Actor with ActorLogging {

  val emailSender = new EmailSender(configuration)

  override def receive: Receive = {
    case flat: Flat => {
      emailSender.sendEmail(flat)
    }
  }
}
