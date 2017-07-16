package actors

import actors.functions.ContentExtractingFunctions._
import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import model.b2b.FlatRequestQuery
import model.b2c.Flat
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import repo.FlatRepo

/**
  * Created by oginskis on 12/03/2017.
  */
class ExtractingActor (flatRepo:FlatRepo,
                       wsClient:WSClient,
                       configuration: Configuration)
  extends Actor with ActorLogging {

  val persistActor = {
    context.actorOf(RoundRobinPool(configuration.get[Int](ExtractingActor.persistingParallelActors))
      .props(Props(classOf[PersistActor],flatRepo,configuration)), name = "persistActor")
  }

  override def receive: Receive = {
    case flatQuery: FlatRequestQuery => {
      Logger.info(s"Checking flats for: $flatQuery")
      val extractedItems = extract(sslv,flatQuery,wsClient,configuration)
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

object ExtractingActor {
  val persistingParallelActors = "actor.system.parallel.actors.persisting"
}
