package actors


import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.mongodb.MongoCommandException
import model.b2b.FlatRequestQuery
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import repo.FlatRepo

import scala.collection.JavaConverters._

/**
  * Created by oginskis on 25/05/2017.
  */
class ProcessingActor (flatRepo:FlatRepo,
                                wsClient:WSClient,
                                configuration: Configuration) extends Actor with ActorLogging {

  val extractingActor = {
    context.actorOf(RoundRobinPool(configuration.get[Int](ProcessingActor.extractingParallelActors))
      .props(Props(classOf[ExtractingActor],flatRepo,wsClient,configuration)), name = "extractingActor")
  }

  override def receive: Receive = {
    case ProcessingActor.Process => {
      val flatSearchRequestConfig = configuration.underlying.getObjectList(ProcessingActor.SearchRequestList).asScala
      flatSearchRequestConfig.foreach(configItem =>
        extractingActor ! (FlatRequestQuery(Option(configItem.get("city").unwrapped.toString),
          Option(configItem.get("district").unwrapped.toString),
          Option(configItem.get("action").unwrapped.toString)
        ),1)
      )
    }
    case ProcessingActor.Expire => {
      try {
        val expireOlderThan = configuration.get[Int](ProcessingActor.ExpireOlderThan)
        flatRepo.expireFlats(expireOlderThan)
      }
      catch {
        case e: MongoCommandException => {
          Logger.info(s"Error during flat expiration on ss.lv: ${e.getErrorMessage}, will retry...")
          self ! ProcessingActor.Expire
        }
      }
    }
  }

}

object ProcessingActor {
  val Process = "processFlats"
  val Expire = "expireFlats"
  val SearchRequestList = "flat.search.request.config"
  val ExpireOlderThan = "flat.expire.olderThan"
  val extractingParallelActors = "actor.system.parallel.actors.extracting"
}
