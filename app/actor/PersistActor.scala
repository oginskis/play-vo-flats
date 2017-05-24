package actor

import akka.actor.{ActorLogging, Actor, ActorRef}
import model.Flat
import play.api.Logger
import repo.FlatRepo

/**
  * Created by oginskis on 12/03/2017.
  */
class PersistActor(notificationActor: ActorRef, flatRepo: FlatRepo) extends Actor with ActorLogging {

  override def receive: Receive = {
    case flat: Flat => {
      val flatStatus = flatRepo.addOrUpdateFlat(flat)
      def matchesFilter(flat: Flat): Boolean = {
        if (flat.price.get < 90000 && flat.size.get >= 40) true
        else false
      }
      if (Flat.Added == flatStatus && matchesFilter(flat)) {
        Logger.info(s"New flat has been found $flat Sending out emails...")
        notificationActor ! flat
      }
    }
  }
}
