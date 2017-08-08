package scala.repo

import configuration.{MockedMongoConnection, MongoConnection}
import model.b2c.{Range, Subscription}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repo.SubscriptionRepo

class SubscriptionRepoTest extends FlatSpec with Matchers with BeforeAndAfterAll {

  val application = {
    new GuiceApplicationBuilder()
      .overrides(bind[MongoConnection].to[MockedMongoConnection])
      .build()
  }

  val subscriptionRepo = application.injector.instanceOf[SubscriptionRepo]

  "Subscription " should " be created" in {
    val subscription = new Subscription(
      subscriber = Option("viktors@gmail.com"),
      priceRange = Option(Range(Option(1),Option(3))),
      floorRange = Option(Range(Option(2),Option(5))),
      sizeRange = Option(Range(Option(40),Option(70))),
      cities = Option(Array[String]("riga","jurmala")),
      districts = Option(Array[String]("centre","teika")),
      actions = Option(Array[String]("sell"))
    )
    subscriptionRepo.createSubscription(subscription)
  }

  var subscriptionId:Option[String] = None

  it should "be found by email" in {
    val subscriptionList = subscriptionRepo.findAllSubscriptionsForEmail("viktors@gmail.com")
    subscriptionList should not be (None)
    subscriptionList.get.size should be (1)
    val subscription = subscriptionList.get.head
    checkSubscriptionObject(subscription)
    subscriptionId = subscription.subscriptionId
  }

  private def checkSubscriptionObject(subscription: Subscription) = {
    subscription.subscriber.get should be("viktors@gmail.com")
    subscription.cities.get should contain("riga")
    subscription.cities.get should contain("jurmala")
    subscription.districts.get should contain("centre")
    subscription.districts.get should contain("teika")
    subscription.actions.get should contain("sell")
    subscription.priceRange.get.from.get should be(1)
    subscription.priceRange.get.to.get should be(3)
    subscription.floorRange.get.from.get should be(2)
    subscription.floorRange.get.to.get should be(5)
    subscription.sizeRange.get.from.get should be(40)
    subscription.sizeRange.get.to.get should be(70)
    subscription.subscriptionId.get should not be (None)
  }

  it should "be found by identifier" in {
    val subscription = subscriptionRepo.getSubscriptionById(subscriptionId.get)
    subscription should not be (None)
    checkSubscriptionObject(subscription.get)
  }

  "Subscription" should "be deleted by it's identifier" in {
    subscriptionRepo.deleteSubscriptionById(subscriptionId.get)
  }

  it should "not be returned by identifier anymore after it has been deleted" in {
    val subscription = subscriptionRepo.getSubscriptionById(subscriptionId.get)
    subscription should be (None)
  }


}
