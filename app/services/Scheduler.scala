package services

import javax.inject.{Inject, Singleton}

import actor.{ExtractingActor, NotificationActor, PersistActor, ProcessingActor}
import akka.actor.{ActorSystem, Props}
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import repo.FlatRepo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by oginskis on 12/03/2017.
  */
@Singleton
class Scheduler @Inject()(appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem,
                          configuration: Configuration, emailSender: EmailSender,
                          wSClient: WSClient, flatRepo: FlatRepo) {

  val notification = actorSystem.actorOf(Props(new NotificationActor(emailSender)), name = "notification")
  val persist = actorSystem.actorOf(Props(new PersistActor(notification, flatRepo)), name = "persist")
  val extracting = actorSystem.actorOf(Props(new ExtractingActor(persist,wSClient,configuration)), name = "extracting")
  val processing = actorSystem.actorOf(Props(new ProcessingActor(extracting,configuration,flatRepo)), name = "processing")
  actorSystem.scheduler.schedule(0 seconds, configuration.underlying.getInt(Scheduler.FLAT_CHECK_SCHEDULE) seconds,
    processing, ProcessingActor.Process)
  actorSystem.scheduler.schedule(0 seconds, configuration.underlying.getInt(Scheduler.EXPIRATION_KICK_OFF_SCHEDULE)
    seconds,
    processing, ProcessingActor.Expire)

}

object Scheduler {
  val FLAT_CHECK_SCHEDULE = "ss.lv.flatCheckSchedule"
  val EXPIRATION_KICK_OFF_SCHEDULE = "flat.expire.kickOffSchedule"
}
