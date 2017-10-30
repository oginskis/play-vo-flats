package scala.repo

import scala.util.Failure
import com.dumbster.smtp.SimpleSmtpServer
import configuration.testsupport.MongoINMemoryDBSupport
import model.b2c.{Flat, Range, Subscription}
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import repo.SubscriptionRepo

import scala.collection.JavaConverters._
import scala.collection.immutable.StringOps
import scala.concurrent.Await
import scala.testhelpers.TestApplicationContextHelper._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.Success

import scala.concurrent.duration._

class SubscriptionRepoTest extends PlaySpec with BeforeAndAfterAll  {

  val fakeSmtp = SimpleSmtpServer.start(2525)

  override def afterAll = {
    MongoINMemoryDBSupport.purgeFlats()
    fakeSmtp.stop()
  }
  var subscriptionId: String = _
  var activationToken: String = _

  "Subscription" should {
    "be created and activated" when {
      "a valid subscription object is passed to the function" in {
        val subscription = new Subscription(
          subscriber = "viktors@gmail.com",
          priceRange = Option(Range(Option(1), Option(3))),
          floorRange = Option(Range(Option(2), Option(5))),
          sizeRange = Option(Range(Option(40), Option(70))),
          buildingTypes = Option(Array[String]("Specpr.")),
          cities = Option(Array[String]("riga", "jurmala")),
          districts = Option(Array[String]("centre", "teika")),
          actions = Option(Array[String]("sell"))
        )
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo].createSubscription(subscription)
        future.map(result => {
          result mustBe true
        })
      }
      "an activation email is received" in {
        Thread.sleep(500)
        val emails = fakeSmtp.getReceivedEmails.asScala
        emails.size mustBe 1
        for (email <- emails.headOption){
          val index = new StringOps(email.getBody).lastIndexOfSlice("/subscription/enable/")
          activationToken = email.getBody.substring(index+21,32+index+21)
        }
      }
      "subscription is activated" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo].enableSubscription(activationToken)
        future.map(enabled => {
          enabled mustBe true
        })
        fakeSmtp.reset
      }
    }
    "be found" when {
      "a valid email is passed to the function" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscriptionsForEmail("viktors@gmail.com")
        future.map(subscriptionList => {
          subscriptionList mustNot be (None)
          subscriptionList.size mustBe 1
          val subscription = subscriptionList.head
          checkSubscriptionObject(subscription)
          subscription.subscriptionId match {
            case Some(value) => subscriptionId = value
            case None => fail("subscriptionId must not be empty")
          }
        })
      }
    }
    "not be found" when {
      "an invalid email is passed to the function" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscriptionsForEmail("viktorsgmail.com")
        future.map(subscriptionList => {
          subscriptionList mustBe List[Subscription]()
        })
      }
      "a valid email is passed to the function, but subscription does not exist for this email" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscriptionsForEmail("doesnotexist@gmail.com")
        future.map(subscriptionList => {subscriptionList.size mustBe 0})
      }
    }
    "be found" when {
      "a valid identifier is passed to the function" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo].getSubscriptionById(subscriptionId)
        future.map(subscription => {
          subscription mustNot be(None)
          subscription.map(checkSubscriptionObject)
        }
        )
      }
    }
    "not be found" when {
      "an invalid identifier is passed to the function" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo].getSubscriptionById("aoao")
        future.map(subscription => {
          subscription mustBe (None)
        }
        )
      }
    }
    "not be found" when {
      "an valid identifier is passed to the function, but subscription does not exist for this identifier" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .getSubscriptionById("123456abcdef123456abcdef")
        future.map(subscription => {
          subscription mustBe (None)
        })
      }
    }
    "be deleted" when {
      "an identifier of previously created subscription is passed to the function" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo].deleteSubscriptionById(subscriptionId)
        future.map(deletedCount => {
          deletedCount mustBe 1
        })
      }
    }
    "be not found" when {
      "an identifier of deleted subscription is passed to the function" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo].getSubscriptionById(subscriptionId)
        future.map(subscription => {
          subscription mustBe None
        })
      }
    }
    "not be deleted" when {
      "a valid identifier is passed to the function, but subscription does not exist for this identifier" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .deleteSubscriptionById("123456abcdef123456abcdef")
        future.map(deletedCount=>{
          deletedCount mustBe 0
        })

      }
    }
    "not be deleted" when {
      "an invalid identifier is passed to the function [IllegalArgumentException must be thrown]" in {
        val future = getGuiceContext.injector.instanceOf[SubscriptionRepo].deleteSubscriptionById("a7d9")
        future onComplete {
          case Success(_) => { fail("Exception must be thrown")}
          case Failure(t) => { t mustBe an [IllegalArgumentException]}
        }
      }
    }
  }
  "Subscriptions" should {
    "be generated as precondition for findAllSubscribersForFlat test (6)" in {
      getGuiceContext.injector.instanceOf[SubscriptionRepo]
        .createSubscription(new Subscription(
          subscriber = "p1@gmail.com",
          priceRange = Option(Range(Option(50000), Option(70000))),
          floorRange = Option(Range(None, Option(5))),
          sizeRange = Option(Range(Option(40), None)),
          buildingTypes = Option(Array[String]("Specpr.")),
          cities = Option(Array[String]("riga", "jurmala")),
          districts = Option(Array[String]("centre", "teika")),
          actions = Option(Array[String]("sell"))
        ))
      getGuiceContext.injector.instanceOf[SubscriptionRepo]
        .createSubscription(new Subscription(
          subscriber = "p2@gmail.com",
          priceRange = Option(Range(Option(60000), Option(80000))),
          floorRange = Option(Range(Option(2),None)),
          sizeRange = Option(Range(Option(70), Option(90))),
          buildingTypes = Option(Array[String]("Specpr.")),
          cities = Option(Array[String]("riga")),
          districts = Option(Array[String]("centre", "teika")),
          actions = Option(Array[String]("sell"))
        ))
      getGuiceContext.injector.instanceOf[SubscriptionRepo]
        .createSubscription(new Subscription(
          subscriber = "p3@gmail.com",
          priceRange = Option(Range(None,Option(79000))),
          floorRange = Option(Range(Option(2),None)),
          sizeRange = Option(Range(Option(70),None)),
          buildingTypes = Option(Array[String]("Hrusc.")),
          cities = Option(Array[String]("riga")),
          districts = None,
          actions = Option(Array[String]("sell"))
        ))
      getGuiceContext.injector.instanceOf[SubscriptionRepo]
        .createSubscription(new Subscription(
          subscriber = "p4@gmail.com",
          priceRange = Option(Range(None,Option(150000))),
          floorRange = Option(Range(Option(3),None)),
          sizeRange = Option(Range(Option(70),None)),
          buildingTypes = Option(Array[String]("Hrusc.")),
          cities = Option(Array[String]("jurmala","riga")),
          districts = None,
          actions = Option(Array[String]("sell"))
        ))
      getGuiceContext.injector.instanceOf[SubscriptionRepo]
        .createSubscription(new Subscription(
          subscriber = "p5@gmail.com",
          priceRange = Option(Range(Option(77000),Option(80000))),
          floorRange = Option(Range(Option(1),Option(3))),
          sizeRange = Option(Range(Option(73),Option(75))),
          buildingTypes = Option(Array[String]("Hrusc.")),
          cities = None,
          districts = Option(Array[String]("centre","teika")),
          actions = Option(Array[String]("sell"))
        ))
      getGuiceContext.injector.instanceOf[SubscriptionRepo]
        .createSubscription(new Subscription(
          subscriber = "p5@gmail.com",
          priceRange = Option(Range(Option(79000),Option(80000))),
          floorRange = Option(Range(None,Option(3))),
          sizeRange = Option(Range(Option(73),Option(75))),
          buildingTypes = Option(Array[String]("103","Hrusc","Specpr.")),
          cities = None,
          districts = Option(Array[String]("centre","teika")),
          actions = Option(Array[String]("sell"))
        ))
      getGuiceContext.injector.instanceOf[SubscriptionRepo]
        .createSubscription(new Subscription(
          subscriber = "p6@gmail.com",
          priceRange = None,
          floorRange = None,
          sizeRange = None,
          buildingTypes = None,
          cities = None,
          districts = None,
          actions = None
        ))
      getGuiceContext.injector.instanceOf[SubscriptionRepo]
        .createSubscription(new Subscription(
          subscriber = "p7@gmail.com",
          priceRange = None,
          floorRange = None,
          sizeRange = None,
          buildingTypes = Option(Array[String]("103","Hrusc","Specpr.")),
          cities = Option(Array[String]("jurmala")),
          districts = Option(Array[String]("vaivari")),
          actions = None
        ))
      getGuiceContext.injector.instanceOf[SubscriptionRepo]
        .createSubscription(new Subscription(
          subscriber = "p8@gmail.com",
          priceRange = None,
          floorRange = None,
          sizeRange = None,
          buildingTypes = Option(Array[String]("103","Hrusc","Specpr.")),
          cities = None,
          districts = None,
          actions = Option(Array[String]("rent"))
        ))
    }
    "not be found unless enabled" in {
      val flat = Flat(
        Option("New"),
        None,
        rooms = Option(4),
        size = Option(75),
        floor = Option(3),
        maxFloors = Option(5),
        price = Option(80000),
        Option("Specpr."),
        None,
        None,
        None,
        Option("riga"),
        Option("teika"),
        Option("sell"),
        Option("false"),
        None,
        None
      )
      val subscriptions = Await.result(getGuiceContext.injector.instanceOf[SubscriptionRepo]
        .findAllSubscribersForFlat(flat),1 second)
      subscriptions.size mustBe 0
    }
    "be found" when {
      "enabled" in {
        Thread.sleep(500)
        fakeSmtp.getReceivedEmails.size mustBe 9
        fakeSmtp.getReceivedEmails.forEach( email => {
            val index = new StringOps(email.getBody).lastIndexOfSlice("/subscription/enable/")
            activationToken = email.getBody.substring(index + 21, 32 + index + 21)
            val enabled = Await.result(getGuiceContext.injector.instanceOf[SubscriptionRepo].enableSubscription(activationToken),1 second)
            enabled mustBe true
          }
        )
      }
      "findAllSubscribersForFlat is kicked of for flat: rooms=4,size=75,floor=3,price=80000,riga,teika,sell (3)" in {
        val flat = Flat(
          Option("New"),
          None,
          rooms = Option(4),
          size = Option(75),
          floor = Option(3),
          maxFloors = Option(5),
          price = Option(80000),
          Option("Specpr."),
          None,
          None,
          None,
          Option("riga"),
          Option("teika"),
          Option("sell"),
          Option("false"),
          None,
          None
        )
        val subscriptions = Await.result(getGuiceContext.injector.instanceOf[SubscriptionRepo]
              .findAllSubscribersForFlat(flat),3 seconds)
       // subscriptions.size mustBe 3
        val subscribers = subscriptions.map(subscription => subscription.subscriber)
        subscribers must contain ("p2@gmail.com")
        subscribers must contain ("p5@gmail.com")
        subscribers must contain ("p6@gmail.com")
      }
      "findAllSubscribersForFlat is kicked of for flat: rooms=2,size=75,floor=2,price=80000,riga,teika,sell (3)" in {
        val flat = Flat(
          Option("New"),
          None,
          rooms = Option(2),
          size = Option(75),
          floor = Option(2),
          maxFloors = Option(5),
          price = Option(80000),
          Option("Specpr."),
          None,
          None,
          None,
          Option("riga"),
          Option("teika"),
          Option("sell"),
          Option("false"),
          None,
          None
        )
        val subscriptions = Await.result(getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscribersForFlat(flat),1 second)
        subscriptions.size mustBe 3
        val subscribers = subscriptions.map(subscription => subscription.subscriber)
        subscribers must contain ("p2@gmail.com")
        subscribers must contain ("p5@gmail.com")
        subscribers must contain ("p6@gmail.com")
      }
      "findAllSubscribersForFlat is kicked of for flat: rooms=2,size=75,floor=4,price=140000,riga,teika,sell (2)" in {
        val flat = Flat(
          Option("New"),
          None,
          rooms = Option(2),
          size = Option(75),
          floor = Option(4),
          maxFloors = Option(5),
          price = Option(140000),
          Option("Hrusc."),
          None,
          None,
          None,
          Option("riga"),
          Option("teika"),
          Option("sell"),
          Option("false"),
          None,
          None
        )
        val subscriptions = Await.result(getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscribersForFlat(flat),1 second)
        subscriptions.size mustBe 2
        val subscribers = subscriptions.map(subscription => subscription.subscriber)
        subscribers must contain ("p4@gmail.com")
        subscribers must contain ("p6@gmail.com")
      }
      "findAllSubscribersForFlat is kicked of for flat: rooms=2,size=75,floor=4,price=140000,jurmala,vaivari,sell (2)" in {
        val flat = Flat(
          Option("New"),
          None,
          rooms = Option(2),
          size = Option(75),
          floor = Option(4),
          maxFloors = Option(5),
          price = Option(70000),
          Option("Specpr."),
          None,
          None,
          None,
          Option("jurmala"),
          Option("vaivari"),
          Option("sell"),
          Option("false"),
          None,
          None
        )
        val subscriptions = Await.result(getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscribersForFlat(flat),1 second)
        subscriptions.size mustBe 2
        val subscribers = subscriptions.map(subscription => subscription.subscriber)
        subscribers must contain ("p7@gmail.com")
        subscribers must contain ("p6@gmail.com")
      }
      "findAllSubscribersForFlat is kicked of for flat: rooms=2,size=75,floor=4,price=300000,jurmala,teika,sell (1)" in {
        val flat = Flat(
          Option("New"),
          None,
          rooms = Option(2),
          size = Option(75),
          floor = Option(4),
          maxFloors = Option(5),
          price = Option(300000),
          Option("Specpr."),
          None,
          None,
          None,
          Option("jurmala"),
          Option("teika"),
          Option("sell"),
          Option("false"),
          None,
          None
        )
        val subscriptions = Await.result(getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscribersForFlat(flat),1 second)
        subscriptions.size mustBe 1
        val subscribers = subscriptions.map(subscription => subscription.subscriber)
        subscribers must contain ("p6@gmail.com")
      }
      "findAllSubscribersForFlat is kicked of for flat: rooms=2,size=75,floor=4,price=300000,jurmala,teika,rent (2)" in {
        val flat = Flat(
          Option("New"),
          None,
          rooms = Option(2),
          size = Option(75),
          floor = Option(4),
          maxFloors = Option(5),
          price = Option(300000),
          Option("Specpr."),
          None,
          None,
          None,
          Option("jurmala"),
          Option("teika"),
          Option("rent"),
          Option("false"),
          None,
          None
        )
        val subscriptions = Await.result(getGuiceContext.injector.instanceOf[SubscriptionRepo]
          .findAllSubscribersForFlat(flat),1 second)
        subscriptions.size mustBe 2
        val subscribers = subscriptions.map(subscription => subscription.subscriber)
        subscribers must contain ("p6@gmail.com")
        subscribers must contain ("p8@gmail.com")
      }
    }
  }

  private def checkSubscriptionObject(subscription: Subscription) = {
    subscription.subscriber mustBe "viktors@gmail.com"
    subscription.language mustBe "en"
    subscription.districts match {
      case Some(list) => {
        list must contain("centre")
        list must contain("teika")
      }
      case None => fail("Districts list must not be empty")
    }
    subscription.actions match {
      case Some(list) => {
        list must contain("sell")
      }
      case None => fail("Actions list must not be empty")
    }
    subscription.cities match {
      case Some(list) => {
        list must contain ("riga")
        list must contain ("jurmala")
      }
      case None => fail("Cities list must not be empty")
    }
    subscription.sizeRange match {
      case Some(range) => {
        range.from mustBe Some(40)
        range.to mustBe Some(70)
      }
      case None => fail("sizeRange must be present")
    }
    subscription.floorRange match {
      case Some(range) => {
        range.from mustBe Some(2)
        range.to mustBe Some(5)
      }
      case None => fail("floorRange must be present")
    }
    subscription.priceRange match {
      case Some(range) => {
        range.from mustBe Some(1)
        range.to mustBe Some(3)
      }
      case None => fail("priceRange must be present")
    }
    subscription.subscriptionId match {
      case Some(value) => Predef.print(value)
      case None => fail("SubscriptionId must not be None")
    }
    subscription.enabled match {
      case Some(value) => value mustBe true
      case None => fail("Enabled flag must not be None")
    }
  }
}
