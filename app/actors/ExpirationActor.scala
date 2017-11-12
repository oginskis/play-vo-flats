package actors

import akka.actor.{Actor, ActorLogging}
import com.mongodb.MongoCommandException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import repo.FlatRepo


/**
  * Created by oginskis on 25/05/2017.
  */
class ExpirationActor(flatRepo:FlatRepo,
                      wsClient:WSClient,
                      configuration: Configuration) extends Actor with ActorLogging {

  override def receive: Receive = {

    case ExpirationActor.Expire => {
      try {
        val expireOlderThan = configuration.get[Int](ExpirationActor.ExpireOlderThan)
        flatRepo.expireFlats(expireOlderThan)
      }
      catch {
        case e: MongoCommandException => {
          Logger.info(s"Error during flat expiration on ss.lv: ${e.getErrorMessage}, will retry...")
          self ! ExpirationActor.Expire
        }
      }
    }
  }

}

object ExpirationActor {
  val Expire = "expireFlats"
  val ExpireOlderThan = "flat.expire.olderThan"

}
