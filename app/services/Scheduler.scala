package services

import javax.inject.{Inject, Singleton}

import actor.{ExtractingActor, NotificationActor, PersistActor, ProcessingActor}
import akka.actor.{ActorSystem, Props}
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import repo.FlatRepo
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by oginskis on 12/03/2017.
  */
@Singleton
class Scheduler @Inject()(appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem,
                          configuration: Configuration, emailSender: EmailSender,
                          flatExtractor: FlatExtractor, flatRepo: FlatRepo) {

  val notification = actorSystem.actorOf(Props(new NotificationActor(emailSender)), name = "notification")
  val persist = actorSystem.actorOf(Props(new PersistActor(notification, flatRepo)), name = "persist")
  val extracting = actorSystem.actorOf(Props(new ExtractingActor(persist, flatExtractor)), name = "extracting")
  val processing = actorSystem.actorOf(Props(new ProcessingActor(extracting,configuration)), name = "processing")
  actorSystem.scheduler.schedule(0 seconds, configuration.underlying.getInt(Scheduler.FLAT_CHECK_SCHEDULE) seconds,
    processing, ProcessingActor.Process)

}

object Scheduler {
  val FLAT_CHECK_SCHEDULE = "ss.lv.flatCheckSchedule"
}
