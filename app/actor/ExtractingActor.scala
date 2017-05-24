package actor

import akka.actor.{ActorLogging, Actor, ActorRef}
import play.api.Logger
import services.FlatExtractor

/**
  * Created by oginskis on 12/03/2017.
  */
class ExtractingActor(persistActor: ActorRef, flatExtractor: FlatExtractor) extends Actor with ActorLogging {

  override def receive: Receive = {
    case ExtractingActor.Extract => {
      Logger.info("Checking flats")
      flatExtractor.extractFlats.foreach(flat => {
        persistActor ! flat
      })
    }
  }
}

object ExtractingActor {
  val Extract = "extract"
}
