package scala.controllers.subscription

import com.dumbster.smtp.{SimpleSmtpServer, SmtpMessage}
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

import scala.collection.immutable.StringOps
import scala.concurrent.Future
import scala.testhelpers.TestApplicationContextHelper._

class SubscriptionControllerTest extends PlaySpec with Results with BeforeAndAfterAll {

  val fakeSmtp = SimpleSmtpServer.start(2525)

  override def afterAll = {
    MongoINMemoryDBSupport.purgeFlats()
    fakeSmtp.stop()
  }

  var subscriptionId: String = _

  "Subscription(s)" should {
    "be created with valid input" when {
      "all data is present" in {
        val body = Json.parse(
          """
                   {
                       "subscriber":"viktors.test1@gmail.lv",
                       "language": "en",
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
                       "language": "lv",
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
                       "language": "en",
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
                       "language": "lv",
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
                       "language": "en",
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
                       "language": "en",
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
      "subscriptions are activated" in {
        fakeSmtp.getReceivedEmails.forEach(email => {
          val activationToken = extractActivationToken(email)
          val url = controllers.subscription.routes.SubscriptionController
            .enableSubscription(activationToken).url
          val result:Future[Result] = prepareGetRequestAndCallApi(activationToken,
              url,
              getGuiceContext().injector.instanceOf[SubscriptionController].enableSubscription(activationToken))
          status(result) mustBe 200
        })
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
        val subscribers = subscriptions.map(subscription => {
            (subscription.subscriptionId match {
              case Some(value) => value
              case None => fail("SubscriptionId must be present on subscription")
            },
              subscription.subscriber
            )
          }
        )
        val emails =  subscribers.map(tuple => tuple._2)
        emails must contain ("viktors.test4@gmail.lv")
        emails must contain ("viktors.test1@gmail.lv")
        val subscriptionIds = subscribers.map(tuple => tuple._1)
        subscriptionId = subscriptionIds.head
      }
    }
    "not be found" when {
      "email of subscriber is passed to the function in wrong format, BAD_REQUEST(400) must be returned" in {
        val url =  controllers.subscription.routes.SubscriptionController
          .getAllSubscriptionsForEmail("viktors.test1gmail.lv").url
        val call = getGuiceContext().injector.instanceOf[SubscriptionController].getAllSubscriptionsForEmail("viktors.test1gmail.lv")
        val result: Future[Result] = prepareGetRequestAndCallApi("viktors.test1gmail.lv",url,call)
        status(result) mustBe 400
      }
      "there is no subscriber with given valid email, NOT_FOUND(404) must be returned" in {
        val url =  controllers.subscription.routes.SubscriptionController
          .getAllSubscriptionsForEmail("nosuchsubscriber@gmail.lv").url
        val call = getGuiceContext().injector.instanceOf[SubscriptionController].getAllSubscriptionsForEmail("nosuchsubscriber@gmail.lv")
        val result: Future[Result] = prepareGetRequestAndCallApi("nosuchsubscriber@gmail.lv",url,call)
        status(result) mustBe 404
      }
      "subscription identifier which is passed to the function was in wrong format, BAD_REQUEST(400) must be returned" in {
        val url =  controllers.subscription.routes.SubscriptionController
          .getAllSubscriptionsForEmail("aaa").url
        val call = getGuiceContext().injector.instanceOf[SubscriptionController].getSubscriptionById("aaa")
        val result: Future[Result] = prepareGetRequestAndCallApi("aaa",url,call)
        status(result) mustBe 400
      }
      "there is no subscriber with given valid identifier, NOT_FOUND(404) must be returned" in {
        val url =  controllers.subscription.routes.SubscriptionController
          .getAllSubscriptionsForEmail("abcdef123456abcdef123456").url
        val call = getGuiceContext().injector.instanceOf[SubscriptionController].getSubscriptionById("abcdef123456abcdef123456")
        val result: Future[Result] = prepareGetRequestAndCallApi("abcdef123456abcdef123456",url,call)
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
        val url =  controllers.subscription.routes.SubscriptionController
          .getAllSubscriptionsForEmail(subscriptionId).url
        val call = getGuiceContext().injector.instanceOf[SubscriptionController].getSubscriptionById(subscriptionId)
        val result: Future[Result] = prepareGetRequestAndCallApi("abcdef123456abcdef123456",url,call)
        status(result) mustBe 404
      }
    }
  }

  private def extractActivationToken(email: SmtpMessage) = {
    val index = new StringOps(email.getBody).lastIndexOfSlice("/subscription/enable/")
    val activationToken = email.getBody.substring(index + 21, 32 + index + 21)
    activationToken
  }

  private def prepareGetRequestAndCallApi(parameter: String, endpointUrl:String, action: Action[AnyContent]) = {
    val request = FakeRequest(
      HttpVerbs.GET,
      endpointUrl
    )
    action.apply(request)
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
