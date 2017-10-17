import javax.inject.{Inject, Singleton}

import actors.{ExtractingActor, ProcessingActor}
import akka.actor.{ActorSystem, Props}
import akka.routing.RoundRobinPool
import play.api.Configuration
import play.api.libs.ws.WSClient
import repo.{FlatRepo, SubscriptionRepo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.services.EmailSendingService
import Scheduler._

/**
  * Created by oginskis on 12/03/2017.
  */
@Singleton
class Scheduler @Inject()(actorSystem: ActorSystem,
                          configuration: Configuration, flatRepo: FlatRepo, subscriptionRepo: SubscriptionRepo,
                          emailSendingService: EmailSendingService, wsClient: WSClient) {
  val processingActor = actorSystem.actorOf((Props(classOf[ProcessingActor],flatRepo,subscriptionRepo,
    emailSendingService,wsClient,configuration)))
  val extractingActor = {
    actorSystem.actorOf(RoundRobinPool(configuration.get[Int](extractingParallelActors))
      .props(Props(classOf[ExtractingActor],flatRepo,subscriptionRepo,emailSendingService,
        wsClient,configuration)))
  }

  actorSystem.scheduler.schedule(0 seconds, configuration.underlying.getInt(FlatCheckSchedule) seconds,
    extractingActor, ExtractingActor.Process)
  actorSystem.scheduler.schedule(0 seconds, configuration.underlying.getInt(EXPIRATION_KICK_OFF_SCHEDULE)
    seconds,
    processingActor, ProcessingActor.Expire)
}

object Scheduler {
  val FlatCheckSchedule = "ss.lv.flatCheckSchedule"
  val EXPIRATION_KICK_OFF_SCHEDULE = "flat.expire.kickOffSchedule"
  val extractingParallelActors = "actor.system.parallel.actors.extracting"
}
