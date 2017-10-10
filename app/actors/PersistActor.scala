package actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.mongodb.MongoCommandException
import model.b2c.Flat
import play.api.{Configuration, Logger}
import repo.{FlatRepo, SubscriptionRepo}

import scala.services.EmailSendingService

/**
  * Created by oginskis on 12/03/2017.
  */
class PersistActor (flatRepo: FlatRepo,
                    subscriptionRepo: SubscriptionRepo, emailSendingService: EmailSendingService,
                    configuration: Configuration) extends Actor with ActorLogging {

  val notificationActor = {
    context.actorOf(RoundRobinPool(configuration.get[Int](PersistActor.notificationParallelActors))
      .props(Props(classOf[NotificationActor],subscriptionRepo,emailSendingService,configuration)), name = "notificationActor")
  }

  override def receive: Receive = {
    case flat: Flat => {
      try {
        val persistedFlat = flatRepo.addOrUpdateFlat(flat)
        if ("New" == persistedFlat.status.get) {
          Logger.info(s"New flat has been found $persistedFlat Sending out emails to subscribers (if any)")
          notificationActor ! persistedFlat
        }
      }
      catch {
        case e: MongoCommandException => {
          val message = e.getErrorMessage
          Logger.error(s"$flat flat insert/update has failed with $message, will retry")
          self ! flat
        }
      }
    }
  }
}

object PersistActor {
  val notificationParallelActors = "actor.system.parallel.actors.notification"
}
