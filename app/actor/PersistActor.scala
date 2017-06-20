package actor

import akka.actor.{ActorLogging, Actor, ActorRef}
import com.mongodb.MongoCommandException
import model.b2c.Flat
import play.api.Logger
import repo.FlatRepo

/**
  * Created by oginskis on 12/03/2017.
  */
class PersistActor(notificationActor: ActorRef, flatRepo: FlatRepo) extends Actor with ActorLogging {

  override def receive: Receive = {
    case flat: Flat => {
      try {
        val persistedFlat = flatRepo.addOrUpdateFlat(flat)
        def matchesFilter(flat: Flat): Boolean = {
          if (flat.price.get < 90000
            && flat.size.get >= 40
          ) true
          else false
        }
        if (Flat.New == persistedFlat.status && matchesFilter(persistedFlat)) {
          Logger.info(s"New flat has been found $persistedFlat Sending out emails...")
          notificationActor ! persistedFlat
        }
      }
      catch {
        case e: MongoCommandException => {
          val message = e.getErrorMessage
          Logger.info(s"$flat flat insert/update has failed with $message, will retry")
          self ! flat
        }
      }
    }
  }

}
