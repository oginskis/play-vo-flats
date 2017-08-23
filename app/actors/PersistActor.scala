package actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.mongodb.MongoCommandException
import model.b2c.Flat
import play.api.{Configuration, Logger}
import repo.FlatRepo

/**
  * Created by oginskis on 12/03/2017.
  */
class PersistActor (flatRepo: FlatRepo,
                    configuration: Configuration) extends Actor with ActorLogging {

  val notificationActor = {
    context.actorOf(RoundRobinPool(configuration.get[Int](PersistActor.notificationParallelActors))
      .props(Props(classOf[NotificationActor],configuration)), name = "notificationActor")
  }

  override def receive: Receive = {
    case flat: Flat => {
      try {
        val persistedFlat = flatRepo.addOrUpdateFlat(flat)
        def matchesFilter(flat: Flat): Boolean = {
          if (flat.price.get < 90000
            && flat.size.get >= 40
            && (flat.district.get == "centre" || flat.district.get == "teika" || flat.district.get == "agenskalns" )
          ) true
          else false
        }
        if ("New" == persistedFlat.status && matchesFilter(persistedFlat)) {
          Logger.info(s"New flat has been found $persistedFlat Sending out emails...")
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
