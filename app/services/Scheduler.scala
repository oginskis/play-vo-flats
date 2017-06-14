package services

import javax.inject.{Inject, Singleton}

import actor.{ExtractingActor, NotificationActor, PersistActor, ProcessingActor}
import akka.actor.{ActorSystem, Props}
import akka.routing.RoundRobinPool
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import repo.FlatRepo

import scala.concurrent.duration._

/**
  * Created by oginskis on 12/03/2017.
  */
@Singleton
class Scheduler @Inject()(appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem,
                          configuration: Configuration, emailSender: EmailSender,
                          wSClient: WSClient, flatRepo: FlatRepo) {

  val notification = actorSystem.actorOf(Props(new NotificationActor(emailSender)).withRouter(
    RoundRobinPool(nrOfInstances = 2)), name = "notification")
  val persist = actorSystem.actorOf(Props(new PersistActor(notification, flatRepo)).withRouter(
    RoundRobinPool(nrOfInstances = 2)), name = "persist")
  val extracting = actorSystem.actorOf(Props(new ExtractingActor(persist,wSClient,configuration)).withRouter(
    RoundRobinPool(nrOfInstances = 2)),
    name = "extracting")
  val processing = actorSystem.actorOf(Props(new ProcessingActor(extracting,configuration)).withRouter(
    RoundRobinPool(nrOfInstances = 2)), name = "processing")
  actorSystem.scheduler.schedule(0 seconds, configuration.underlying.getInt(Scheduler.FLAT_CHECK_SCHEDULE) seconds,
    processing, ProcessingActor.Process)

}

object Scheduler {
  val FLAT_CHECK_SCHEDULE = "ss.lv.flatCheckSchedule"
}
