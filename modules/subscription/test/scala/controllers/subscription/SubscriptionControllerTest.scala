package scala.controllers.subscription

import configuration.testsupport.MongoINMemoryDBSupport
import controllers.subscription.SubscriptionController
import model.b2c.Subscription
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import play.api.http.HttpVerbs
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest, Helpers}

import scala.concurrent.Future
import scala.testhelpers.TestApplicationContextHelper._

class SubscriptionControllerTest extends PlaySpec with Results with BeforeAndAfterAll {

  override def afterAll = {
    MongoINMemoryDBSupport.purgeFlats()
  }

  var subscriptionId: String = _

  "Subscription(s)" should {
    "be created with valid input" when {
      "all data is present" in {
        val body = Json.parse(
          """
                   {
                       "subscriber":"viktors.test1@gmail.lv",
                       "priceRange": {
                          "from": 50000,
                          "to": 70000
                        },
                       "sizeRange": {
                          "from": 40,
                          "to": 70
                        },
                       "floorRange": {
                          "from": 3,
                          "to": 5
                       },
                       "cities": [
                          "riga",
                          "jurmala"
                       ],
                       "districts": [
                          "centre",
                          "teika"
                       ],
                       "actions": [
                        "sell"
                       ]
                   }
                     """)
        val result: Future[Result] = prepareRequestAndCallApi(
          body,
          controllers.subscription.routes.SubscriptionController.createSubscription().url,
          getGuiceContext().injector.instanceOf[SubscriptionController].createSubscription()
        )
        status(result) mustBe 201
      }
      "ranges are missing" in {
        val body = Json.parse(
          """
                   {
                       "subscriber":"viktors.test2@gmail.lv",
                       "cities": [
                          "jurmala"
                       ],
                       "districts": [
                          "centre",
                          "teika"
                       ],
                       "actions": [
                        "sell"
                       ]
                   }
                     """)
        val result: Future[Result] = prepareRequestAndCallApi(
          body,
          controllers.subscription.routes.SubscriptionController.createSubscription().url,
          getGuiceContext().injector.instanceOf[SubscriptionController].createSubscription()
        )
        status(result) mustBe 201
      }
      "some string collections are missing" in {
        val body = Json.parse(
          """
                   {
                       "subscriber":"viktors.test3@gmail.lv",
                       "priceRange": {
                          "from": 75000,
                          "to": 80000
                        },
                       "sizeRange": {
                          "from": 40,
                          "to": 60
                        },
                       "floorRange": {
                          "from": 2,
                          "to": 5
                       },
                       "actions": [
                        "sell"
                       ]
                   }
                     """)
        val result: Future[Result] = prepareRequestAndCallApi(
          body,
          controllers.subscription.routes.SubscriptionController.createSubscription().url,
          getGuiceContext().injector.instanceOf[SubscriptionController].createSubscription()
        )
        status(result) mustBe 201
      }
      "part of the ranges is missing" in {
        val body = Json.parse(
          """
                   {
                       "subscriber":"viktors.test4@gmail.lv",
                       "priceRange": {
                          "from": 50000
                        },
                       "sizeRange": {
                          "to": 60
                        },
                       "floorRange": {
                          "from": 2
                       },
                       "actions": [
                        "sell"
                       ]
                   }
                     """)
        val result: Future[Result] = prepareRequestAndCallApi(
          body,
          controllers.subscription.routes.SubscriptionController.createSubscription().url,
          getGuiceContext().injector.instanceOf[SubscriptionController].createSubscription()
        )
        status(result) mustBe 201
      }
    }
    "not be created" when {
      "email (subscriber) format is not correct, BAD_REQUEST (400) must be returned" in {
        val body = Json.parse(
          """
                   {
                       "subscriber":"qwerty",
                       "priceRange": {
                          "from": 2,
                          "to": 3
                        },
                       "sizeRange": {
                          "from": 40,
                          "to": 70
                        },
                       "floorRange": {
                          "from": 2,
                          "to": 3
                       },
                       "cities": [
                          "riga",
                          "jurmala"
                       ],
                       "districts": [
                          "centre",
                          "teika"
                       ],
                       "actions": [
                        "sell"
                       ]
                   }
                     """)
        val result: Future[Result] = prepareRequestAndCallApi(
          body,
          controllers.subscription.routes.SubscriptionController.createSubscription().url,
          getGuiceContext().injector.instanceOf[SubscriptionController].createSubscription()
        )
        status(result) mustBe 400
        val responseBody = Json.parse(contentAsString(result))
        (responseBody \ "errorName").as[String] mustBe "Invalid request"
        (responseBody \ "errorDescription").as[String] mustBe "Invalid value in [subscriber] field"
      }
      "one of values in the ranges is not numeric, BAD_REQUEST (400) must be returned" in {
        val body = Json.parse(
          """
                   {
                       "subscriber":"viktors.test@gmail.lv",
                       "priceRange": {
                          "from": "2",
                          "to": 3
                        },
                       "sizeRange": {
                          "from": 40,
                          "to": 70
                        },
                       "floorRange": {
                          "from": 2,
                          "to": 5
                       },
                       "cities": [
                          "riga",
                          "jurmala"
                       ],
                       "districts": [
                          "centre",
                          "teika"
                       ],
                       "actions": [
                        "sell"
                       ]
                   }
                     """)

        val result: Future[Result] = prepareRequestAndCallApi(
          body,
          controllers.subscription.routes.SubscriptionController.createSubscription().url,
          getGuiceContext().injector.instanceOf[SubscriptionController].createSubscription()
        )
        status(result) mustBe 400
        val responseBody = Json.parse(contentAsString(result))
        (responseBody \ "errorName").as[String] mustBe "Invalid request"
        (responseBody \ "errorDescription").as[String] mustBe "Invalid value in [priceRange/from] field"
      }
    }
    "be found" when {
      "email of subscriber is passed to the corresponding function" in {
        val result: Future[Result] = callGetAllSubscriptionsForEmail("viktors.test1@gmail.lv")
        status(result) mustBe 200
        val responseBody = Json.parse(contentAsString(result))
        val subscriptions = responseBody.as[List[Subscription]]
        subscriptions.size mustBe 1
        val subscription = subscriptions.head
        subscription.subscriptionId match {
          case Some(value) => subscriptionId = value
          case None => fail("SubscriptionId must be present")
        }
        validateSubscription(subscription)
      }
      "subscription identifier is passed to the corresponding function" in {
        val result: Future[Result] = callGetSubscriptionById(subscriptionId)
        val responseBody = Json.parse(contentAsString(result))
        validateSubscription(responseBody.as[Subscription])
      }
      "flat entity is passed to the corresponding function" in {
        val body = Json.parse(
          """
            {
            	"price": 70000,
            	"size": 54,
            	"floor": 3,
            	"city": "riga",
            	"district": "centre",
            	"action": "sell"
            }
          """)
        val result: Future[Result] = prepareRequestAndCallApi(
          body,
          controllers.subscription.routes.SubscriptionController.getAllWhoSubscribedFor().url,
          getGuiceContext().injector.instanceOf[SubscriptionController].getAllWhoSubscribedFor()
        )
        status(result) mustBe 200
        val subscriptions = Json.parse(contentAsString(result)).as[List[Subscription]]
        subscriptions.size mustBe 2
        val subscribers = subscriptions.map(subscription => subscription.subscriber)
        subscribers must contain ("viktors.test4@gmail.lv")
        subscribers must contain ("viktors.test1@gmail.lv")
      }
    }
    "not be found" when {
      "email of subscriber is passed to the function in wrong format, BAD_REQUEST(400) must be returned" in {
        val result: Future[Result] = callGetAllSubscriptionsForEmail("viktors.test1gmail.lv")
        status(result) mustBe 400
      }
      "there is no subscriber with given valid email, NOT_FOUND(404) must be returned" in {
        val result: Future[Result] = callGetAllSubscriptionsForEmail("nosuchsubscriber@gmail.lv")
        status(result) mustBe 404
      }
      "subscription identifier which is passed to the function was in wrong format, BAD_REQUEST(400) must be returned" in {
        val result: Future[Result] = callGetSubscriptionById("aaa")
        status(result) mustBe 400
      }
      "there is no subscriber with given valid identifier, NOT_FOUND(404) must be returned" in {
        val result: Future[Result] = callGetSubscriptionById("abcdef123456abcdef123456")
        status(result) mustBe 404
      }
    }
    "be deleted" when {
      "valid identifier was passed to the function" in {
        val result: Future[Result] = callDeleteSubscriptionById(subscriptionId)
        status(result) mustBe 200
      }
    }
    "not be deleted" when {
      "identifier with invalid format was passed to the function, BAD_REQUEST(400) must be " +
        "returned" in {
        val result: Future[Result] = callDeleteSubscriptionById("aaa")
        status(result) mustBe 400
      }
      "no subscription matching valid identifier was found, NOT_FOUND(404 must be returned " in {
        val result: Future[Result] = callDeleteSubscriptionById("123456abcdef123456abcdef")
        status(result) mustBe 404
      }
    }
    "not be found by identifier anymore" when {
      "it was deleted, NOT_FOUND(404) must be returned" in {
        val result: Future[Result] = callGetSubscriptionById(subscriptionId)
        status(result) mustBe 404
      }
    }
  }

  private def callGetAllSubscriptionsForEmail(email: String) = {
    val request = FakeRequest(
      HttpVerbs.GET,
      controllers.subscription.routes.SubscriptionController
        .getAllSubscriptionsForEmail(email).url,
    )
    val controller = getGuiceContext().injector.instanceOf[SubscriptionController]
    val result: Future[Result] = controller
      .getAllSubscriptionsForEmail(email).apply(request)
    result
  }

  private def callGetSubscriptionById(id: String) = {
    val request = FakeRequest(
      HttpVerbs.GET,
      controllers.subscription.routes.SubscriptionController
        .getSubscriptionById(id).url,
    )
    val controller = getGuiceContext().injector.instanceOf[SubscriptionController]
    val result: Future[Result] = controller
      .getSubscriptionById(id).apply(request)
    result
  }

  private def callDeleteSubscriptionById(id: String) = {
    val request = FakeRequest(
      HttpVerbs.DELETE,
      controllers.subscription.routes.SubscriptionController
        .deleteSubscriptionById(id).url,
    )
    val controller = getGuiceContext().injector.instanceOf[SubscriptionController]
    val result: Future[Result] = controller
      .deleteSubscriptionById(id).apply(request)
    result
  }

  private def validateSubscription(subscription: Subscription) = {
    subscription.subscriptionId mustNot be (None)
    subscription.subscriber mustBe "viktors.test1@gmail.lv"
    subscription.priceRange match {
      case Some(range) => {
        range.from mustBe Some(50000)
        range.to mustBe Some(70000)
      }
      case None => fail("priceRange must be present")
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
        range.from mustBe Some(3)
        range.to mustBe Some(5)
      }
      case None => fail("floorRange must be present")
    }
    subscription.cities match {
      case Some(list) => {
        list must contain ("riga")
        list must contain ("jurmala")
      }
      case None => fail("Cities list must not be empty")
    }
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
    subscription.enabled mustBe Option(false)
    subscription.lastUpdatedDateTime mustNot be (None)
  }

  private def prepareRequestAndCallApi(body: JsValue, endpointUrl: String,
                                       action:Action[JsValue]): Future[Result] = {
    val request = FakeRequest(
      Helpers.POST,
      endpointUrl,
      FakeHeaders(Seq.empty),
      body
    )
    action.apply(request)
  }

}
