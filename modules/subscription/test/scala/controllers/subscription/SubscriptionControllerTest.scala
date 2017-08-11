package scala.controllers.subscription

import controllers.subscription.SubscriptionController
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest, Helpers}

import scala.concurrent.Future
import scala.testhelpers.TestApplicationContextHelper._

class SubscriptionControllerTest extends PlaySpec with Results {

  "Subscription" should  {
    "be created with valid input (all data is present)" in {
      val body = Json.parse("""
                   {
                       "subscriber":"viktors.test1@gmail.lv",
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
      val result: Future[Result] = prepareRequestAndCallAPI(body)
      status(result) mustBe (201)
    }

    "be created with valid input (ranges are missing)" in {
      val body = Json.parse("""
                   {
                       "subscriber":"viktors.test2@gmail.lv",

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
      val result: Future[Result] = prepareRequestAndCallAPI(body)
      status(result) mustBe (201)
    }

    "be created with valid input (some string collections are missing)" in {
      val body = Json.parse("""
                   {
                       "subscriber":"viktors.test3@gmail.lv",
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
                          "to": 5
                       },
                       "actions": [
                        "sell"
                       ]
                   }
                     """)
      val result: Future[Result] = prepareRequestAndCallAPI(body)
      status(result) mustBe (201)
    }

    "be created with valid input (part of the ranges is missing)" in {
      val body = Json.parse("""
                   {
                       "subscriber":"viktors.test4@gmail.lv",
                       "priceRange": {
                          "from": 2
                        },
                       "sizeRange": {
                          "to": 70
                        },
                       "floorRange": {
                          "from": 2
                       },
                       "actions": [
                        "sell"
                       ]
                   }
                     """)
      val result: Future[Result] = prepareRequestAndCallAPI(body)
      status(result) mustBe (201)
    }

    "not be created if email (subscriber) format is not correct" in {
      val body = Json.parse("""
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

      val result: Future[Result] = prepareRequestAndCallAPI(body)
      status(result) mustBe (400)
      val responseBody = Json.parse(contentAsString(result))
      (responseBody \ "errorName").as[String] mustBe "Invalid request"
      (responseBody \ "errorDescription").as[String] mustBe "Invalid value in [subscriber] field"
    }

    "not be created if one of values in the ranges is not numeric" in {
      val body = Json.parse("""
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

      val result: Future[Result] = prepareRequestAndCallAPI(body)
      status(result) mustBe (400)
      val responseBody = Json.parse(contentAsString(result))
      (responseBody \ "errorName").as[String] mustBe "Invalid request"
      (responseBody \ "errorDescription").as[String] mustBe "Invalid value in [priceRange/from] field"
    }

  }

  private def prepareRequestAndCallAPI(body: JsValue) = {
    val request = FakeRequest(
      Helpers.POST,
      controllers.subscription.routes.SubscriptionController.createSubscription.url,
      FakeHeaders(Seq.empty),
      body
    )
    val controller = getGuiceContext.injector.instanceOf[SubscriptionController]
    val result: Future[Result] = controller.createSubscription().apply(request)
    result
  }
}
