package actor

import akka.actor.{ActorLogging, Actor, ActorRef}
import play.api.Logger
import services.FlatExtractor

/**
  * Created by oginskis on 12/03/2017.
  */
class ExtractingActor(persistActor: ActorRef, flatExtractor: FlatExtractor) extends Actor with ActorLogging {

  override def receive: Receive = {
    case district: String => {
      Logger.info("Checking flats")
      flatExtractor.extractFlats(district).foreach(flat => {
        persistActor ! flat
      })
    }
  }
}

