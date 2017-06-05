package actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import model.b2b.FlatRequestQuery
import model.b2c.Flat
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import services.functions.ContentExtractingFunctions._

/**
  * Created by oginskis on 12/03/2017.
  */
class ExtractingActor(persistActor: ActorRef, wSClient: WSClient, configuration: Configuration)
  extends Actor with ActorLogging {

  override def receive: Receive = {
    case flatQuery: FlatRequestQuery => {
      Logger.info(s"Checking flats for: $flatQuery")
      val extractedItems = extract(sslv,flatQuery,wSClient,configuration)
      val numberOfItems = extractedItems.size
      Logger.info(s"Number of extracted items for $flatQuery is $numberOfItems")
      extractedItems.foreach(flat => {
        persistActor ! flat
      })
    }
  }

  private def extract(extractingFunction:(FlatRequestQuery,WSClient,Configuration) => List[Flat]
              ,flatRequestQuery: FlatRequestQuery, wSClient: WSClient, configuration: Configuration) = {
    extractingFunction(flatRequestQuery,wSClient,configuration)
  }
}

