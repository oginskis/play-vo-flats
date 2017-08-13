package scala.repo

import model.b2c.{Range, Subscription}
import org.scalatestplus.play.PlaySpec
import repo.SubscriptionRepo

import scala.testhelpers.TestApplicationContextHelper._

class SubscriptionRepoTest extends PlaySpec {

  var subscriptionId: String = _

  "Subscription" should {
    "be created" when {
      "a valid subscription object is passed to the function" in {
        val subscription = new Subscription(
          subscriber = "viktors@gmail.com",
          priceRange = Option(Range(Option(1), Option(3))),
          floorRange = Option(Range(Option(2), Option(5))),
          sizeRange = Option(Range(Option(40), Option(70))),
          cities = Option(Array[String]("riga", "jurmala")),
          districts = Option(Array[String]("centre", "teika")),
          actions = Option(Array[String]("sell"))
        )
        getGuiceContext.injector.instanceOf[SubscriptionRepo].createSubscription(subscription)
      }
    }
    "be found" when {
      "a valid email is passed to the function" in {
        val subscriptionList = getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscriptionsForEmail("viktors@gmail.com")
        subscriptionList mustNot be (None)
        subscriptionList.get.size mustBe 1
        val subscription = subscriptionList.get.head
        checkSubscriptionObject(subscription)
        subscriptionId = subscription.subscriptionId.get
      }
    }
    "not be found" when {
      "an invalid email is passed to the function" in {
        val subscriptionList = getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscriptionsForEmail("viktorsgmail.com")
        subscriptionList mustBe None
      }
      "a valid email is passed to the function, but subscription does not exist for this email" in {
        val subscriptionList = getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscriptionsForEmail("doesnotexist@gmail.com")
        subscriptionList.get.size mustBe 0
      }
    }
    "be found" when {
      "a valid identifier is passed to the function" in {
        val subscription = getGuiceContext.injector.instanceOf[SubscriptionRepo].getSubscriptionById(subscriptionId)
        subscription mustNot be (None)
        checkSubscriptionObject(subscription.get)
      }
    }
    "not be found" when {
      "an invalid identifier is passed to the function" in {
        val subscription = getGuiceContext.injector.instanceOf[SubscriptionRepo].getSubscriptionById("aoao")
        subscription mustBe (None)
      }
    }
    "not be found" when {
      "an valid identifier is passed to the function, but subscription does not exist for this identifier" in {
        val subscription = getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .getSubscriptionById("123456abcdef123456abcdef")
        subscription mustBe (None)
      }
    }
    "be deleted" when {
      "an identifier of previously created subscription is passed to the function" in {
        val deletedCount = getGuiceContext.injector.instanceOf[SubscriptionRepo].deleteSubscriptionById(subscriptionId)
        deletedCount mustBe 1
      }
    }
    "be not found" when {
      "an identifier of deleted subscription is passed to the function" in {
        val subscription = getGuiceContext.injector.instanceOf[SubscriptionRepo].getSubscriptionById(subscriptionId)
        subscription mustBe None
      }
    }
    "not be deleted" when {
      "a valid identifier is passed to the function, but subscription does not exist for this identifier" in {
        val deletedCount = getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .deleteSubscriptionById("123456abcdef123456abcdef")
        deletedCount mustBe 0
      }
    }
    "not be deleted" when {
      "an invalid identifier is passed to the function [IllegalArgumentException must be thrown]" in {
        an [IllegalArgumentException] mustBe thrownBy(getGuiceContext.injector.instanceOf[SubscriptionRepo]
            .deleteSubscriptionById("a7d9"))
      }
    }
  }

  private def checkSubscriptionObject(subscription: Subscription) = {
    subscription.subscriber mustBe "viktors@gmail.com"
    subscription.cities.get must contain("riga")
    subscription.cities.get must contain("jurmala")
    subscription.districts.get must contain("centre")
    subscription.districts.get must contain("teika")
    subscription.actions.get must contain("sell")
    subscription.priceRange.get.from.get mustBe 1
    subscription.priceRange.get.to.get mustBe 3
    subscription.floorRange.get.from.get mustBe 2
    subscription.floorRange.get.to.get mustBe 5
    subscription.sizeRange.get.from.get mustBe 40
    subscription.sizeRange.get.to.get mustBe 70
    subscription.subscriptionId.get mustNot be (None)
  }
}
