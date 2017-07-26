package actors

import actors.functions.ContentExtractingFunctions
import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import model.b2b.FlatRequestQuery
import model.b2c.Flat
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import actors.functions.ContentExtractingFunctions._
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
      .props(Props(classOf[PersistActor], flatRepo, configuration)), name = "persistActor")
  }

  override def receive: Receive = {
    case (flatQuery: FlatRequestQuery, page: Int) => {
      Logger.info(s"Extracting Actor: Checking flats for: $flatQuery, page $page")
      try {
        val flats = extractFlatsFromPage(flatQuery, page, wsClient, configuration)
        if (!flats.isEmpty) {
          flats.foreach(flat => {
            self ! flat
          })
          self ! (flatQuery, page+1)
        }
      }
      catch {
        case e: Exception => {
          Logger.error(s"Error getting flats for $flatQuery and page $page: ${e.getMessage}. Will retry...")
          self ! (flatQuery,page)
        }
      }
    }
    case (flat: Flat) => {
      try {
        val extractedFlat = new Flat(flat.address,
          flat.rooms,
          flat.size,
          flat.floor,
          flat.maxFloors,
          flat.price,
          flat.link,
          flat.city,
          flat.district,
          flat.action,
          extractFlatContactDetails(
            configuration.get[String](ContentExtractingFunctions.SsLvBaseUrl) + flat.link.get,
            wsClient,configuration))
        persistActor ! extractedFlat
      }
      catch {
        case e: Exception => {
          Logger.error(s"Error getting contact details for $flat: ${e.getMessage}. Will retry...")
          self ! flat
        }
      }
    }
  }
}

object ExtractingActor {
  val persistingParallelActors = "actor.system.parallel.actors.persisting"
}

