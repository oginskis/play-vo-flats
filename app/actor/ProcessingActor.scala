package actor


import model.b2b.FlatRequestQuery

import scala.collection.JavaConverters._
import akka.actor.{Actor, ActorLogging, ActorRef}
import play.api.Configuration

/**
  * Created by oginskis on 25/05/2017.
  */
class ProcessingActor(extractingActor: ActorRef, configuration: Configuration) extends Actor with ActorLogging {
  override def receive: Receive = {
    case ProcessingActor.Process => {
      val flatSearchRequestConfig = configuration.underlying.getObjectList("flat.search.request.config").asScala
      flatSearchRequestConfig.foreach(configItem =>
        extractingActor ! new FlatRequestQuery(Option(configItem.get("city").unwrapped.toString),
          Option(configItem.get("district").unwrapped.toString),
          Option(configItem.get("action").unwrapped.toString)
        )
      )
    }
  }


}

object ProcessingActor {
  val Process = "process"
}
