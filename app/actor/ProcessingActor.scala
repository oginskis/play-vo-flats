package actor


import com.mongodb.MongoCommandException
import model.b2b.FlatRequestQuery
import repo.FlatRepo

import scala.collection.JavaConverters._
import akka.actor.{Actor, ActorLogging, ActorRef}
import play.api.{Logger, Configuration}

/**
  * Created by oginskis on 25/05/2017.
  */
class ProcessingActor(extractingActor: ActorRef, configuration: Configuration,
                      flatRepo:FlatRepo) extends Actor with ActorLogging {
  override def receive: Receive = {
    case ProcessingActor.Process => {
      val flatSearchRequestConfig = configuration.underlying.getObjectList(ProcessingActor.SearchRequestList).asScala
      flatSearchRequestConfig.foreach(configItem =>
        extractingActor ! new FlatRequestQuery(Option(configItem.get("city").unwrapped.toString),
          Option(configItem.get("district").unwrapped.toString),
          Option(configItem.get("action").unwrapped.toString)
        )
      )
    }
    case ProcessingActor.Expire => {
      try {
        val expireOlderThan = configuration.underlying.getInt(ProcessingActor.ExpireOlderThan)
        flatRepo.expireFlats(expireOlderThan)
      }
      catch {
        case e: MongoCommandException => {
          Logger.info("Error during flat expiration on ss.lv, retrying..")
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
}
