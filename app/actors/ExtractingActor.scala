package actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import model.b2b.FlatRequestQuery
import model.b2c.Flat
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import repo.{FlatRepo, SubscriptionRepo}
import functions.{ContactDetailsExtractor, FlatExtractor}
import functions.FlatExtractor._
import scala.collection.JavaConverters._

import scala.services.EmailSendingService
import ExtractingActor._

/**
  * Created by oginskis on 12/03/2017.
  */
class ExtractingActor (flatRepo:FlatRepo,
                       subscriptionRepo: SubscriptionRepo,
                       emailSendingService: EmailSendingService,
                       wsClient:WSClient,
                       configuration: Configuration)
  extends Actor with ActorLogging {

  val extractContactDetails = new ContactDetailsExtractor(wsClient,configuration)
  val extractFlats = new FlatExtractor(wsClient, configuration)

  val persistActor = {
    context.actorOf(RoundRobinPool(configuration.get[Int](ExtractingActor.persistingParallelActors))
      .props(Props(classOf[PersistActor],flatRepo,subscriptionRepo,emailSendingService,configuration)), name = "persistActor")
  }

  override def receive: Receive = {
    // start extraction of given cities and districts
    case Process => {
      val flatSearchRequestConfig = configuration.underlying.getObjectList(SearchRequestList).asScala
      flatSearchRequestConfig.foreach(configItem =>
        self ! (FlatRequestQuery(Option(configItem.get("city").unwrapped.toString),
          Option(configItem.get("district").unwrapped.toString),
          Option(configItem.get("action").unwrapped.toString)
        ),1)
      )
    }
    // extract given given page of flats that matches flat request query
    case (flatQuery: FlatRequestQuery, page: Int) => {
      Logger.info(s"Extracting Actor: Checking flats for: $flatQuery, page $page")
      try {
        val flats = extractFlats(flatQuery, page)
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
    // add seller contact details to the flat object, trigger persist flat
    case (flat: Flat) => {
      try {
        val extractedFlat = new Flat(address = flat.address,
          rooms = flat.rooms,
          size = flat.size,
          floor = flat.floor,
          maxFloors = flat.maxFloors,
          price = flat.price,
          buildingType = flat.buildingType,
          link = flat.link,
          city = flat.city,
          district = flat.district,
          action = flat.action,
          contactDetails = extractContactDetails(
            configuration.get[String](SsLvBaseUrl) + flat.link.get))
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
  val Process = "processFlats"
  val SearchRequestList = "flat.search.request.config"
  val extractingParallelActors = "actor.system.parallel.actors.extracting"
}

