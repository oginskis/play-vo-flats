package actor

import akka.actor.{Actor, ActorLogging, ActorRef}

/**
  * Created by oginskis on 25/05/2017.
  */
class ProcessingActor(extractingActor: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case ProcessingActor.Process => {
      extractingActor ! "/riga/centre/sell"
      extractingActor ! "/riga/agenskalns/sell"
      extractingActor ! "/riga/teika/sell"
    }
  }
}

object ProcessingActor {
  val Process = "process"
}
