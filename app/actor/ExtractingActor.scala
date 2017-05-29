package actor

import akka.actor.{ActorLogging, Actor, ActorRef}
import model.b2b.FlatRequestQuery
import play.api.Logger
import services.FlatExtractor

/**
  * Created by oginskis on 12/03/2017.
  */
class ExtractingActor(persistActor: ActorRef, flatExtractor: FlatExtractor) extends Actor with ActorLogging {

  override def receive: Receive = {
    case flatQuery: FlatRequestQuery => {
      Logger.info(s"Checking flats for: $flatQuery")
      val extractedItems = flatExtractor.extractFlats(flatQuery)
      val numberOfItems = extractedItems.size
      Logger.info(s"Number of extracted items for $flatQuery is $numberOfItems")
      extractedItems.foreach(flat => {
        persistActor ! flat
      })
    }
  }
}

