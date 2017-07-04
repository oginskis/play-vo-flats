package actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by oginskis on 02/07/2017.
  */
class ProcessingActorTest extends TestKit(ActorSystem("testActorSystem")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Processing actors" must {
    "Kick off Extracting Actor" in {
      val processingActor = system.actorOf(TestActors.echoActorProps)
      processingActor ! "echo"
    }
  }

}
