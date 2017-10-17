package actors

import akka.actor.{Actor, ActorLogging}
import com.mongodb.MongoCommandException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import repo.FlatRepo


/**
  * Created by oginskis on 25/05/2017.
  */
class ProcessingActor (flatRepo:FlatRepo,
                                wsClient:WSClient,
                                configuration: Configuration) extends Actor with ActorLogging {

  override def receive: Receive = {

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
  val Expire = "expireFlats"
  val ExpireOlderThan = "flat.expire.olderThan"

}
