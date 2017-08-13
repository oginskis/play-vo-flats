package scala.repo.helpers

import java.util

import model.b2c.Subscription
import org.bson.Document
import org.scalatestplus.play.PlaySpec
import repo.helpers.SubscriptionRepoHelper._

class SubscriptionRepoHelperTest extends PlaySpec {

  "Subscription document object" should {
    "be created out of subscription domain object" when {
      "all fields are present" in {
        val subscription = new Subscription(
          subscriber = "viktors@gmail.com",
          priceRange = Option(new model.b2c.Range(Option(1), Option(3))),
          sizeRange = Option(new model.b2c.Range(Option(2), Option(5))),
          floorRange = Option(new model.b2c.Range(Option(2), Option(6))),
          cities = Option(Array[String]("riga", "jurmala")),
          districts = Option(Array[String]("centrs")),
          actions = Option(Array[String]("sell"))
        )
        val doc: Document = createSubscriptionDocument(subscription)
        doc.get("subscriber").toString mustBe "viktors@gmail.com"
        val priceRange = doc.get("priceRange").asInstanceOf[Document]
        priceRange.get("from") mustBe 1
        priceRange.get("to") mustBe 3
        val sizeRange = doc.get("sizeRange").asInstanceOf[Document]
        sizeRange.get("from") mustBe 2
        sizeRange.get("to") mustBe 5
        val floorRange = doc.get("floorRange").asInstanceOf[Document]
        floorRange.get("from") mustBe 2
        floorRange.get("to") mustBe 6
        val cities = doc.get("cities").asInstanceOf[java.util.ArrayList[String]]
        cities.contains("riga") mustBe true
        cities.contains("jurmala") mustBe true
        val districts = doc.get("districts").asInstanceOf[java.util.ArrayList[String]]
        districts.contains("centrs") mustBe true
        val actions = doc.get("actions").asInstanceOf[java.util.ArrayList[String]]
        actions.contains("sell") mustBe true
      }
      "all fields except subscriber field are empty" in {
        val subscription = new Subscription(
          subscriber = "viktors@gmail.com",
          priceRange = None,
          sizeRange = None,
          floorRange = None,
          cities = None,
          districts = None,
          actions = None
        )
        val doc = createSubscriptionDocument(subscription)
        doc.get("subscriber").toString mustBe "viktors@gmail.com"
        doc.get("priceRange") mustBe null
        doc.get("sizeRange") mustBe null
        doc.get("floorRange") mustBe null
        doc.get("cities") mustBe null
        doc.get("districts") mustBe null
        doc.get("actions") mustBe null
      }
      "some fields are empty" in {
        val subscription = new Subscription(
          subscriber = "viktors@gmail.com",
          priceRange = Option(new model.b2c.Range(Option(1), None)),
          sizeRange = Option(new model.b2c.Range(None, Option(5))),
          floorRange = Option(new model.b2c.Range(Option(2), None)),
          cities = None,
          districts = Option(Array[String]("centrs")),
          actions = None
        )
        val doc: Document = createSubscriptionDocument(subscription)
        doc.get("subscriber").toString mustBe "viktors@gmail.com"
        val priceRange = doc.get("priceRange").asInstanceOf[Document]
        priceRange.get("from") mustBe 1
        priceRange.get("to") mustBe null
        val sizeRange = doc.get("sizeRange").asInstanceOf[Document]
        sizeRange.get("from") mustBe null
        sizeRange.get("to") mustBe 5
        val floorRange = doc.get("floorRange").asInstanceOf[Document]
        floorRange.get("from") mustBe 2
        floorRange.get("to") mustBe null
        doc.get("cities") mustBe null
        val districts = doc.get("districts").asInstanceOf[java.util.ArrayList[String]]
        districts.contains("centrs") mustBe true
        doc.get("actions") mustBe null
      }
    }
  }

  "Subscription domain object" should {
    "be created out of subscription document object" when {
      "all fields are present" in {
        val params = new java.util.HashMap[String, Object]()
        params.put("_id","abcdef123456abcdef123456")
        params.put("subscriber","viktors@gmail.com")
        params.put("priceRange",createRangeDocument(Option(1),Option(5)))
        params.put("sizeRange",createRangeDocument(Option(2),Option(6)))
        params.put("floorRange",createRangeDocument(Option(3),Option(10)))
        params.put("cities",new util.ArrayList[String](util.Arrays.asList("riga","jurmala")))
        params.put("districts",new util.ArrayList[String](util.Arrays.asList("centre","teika")))
        params.put("actions",new util.ArrayList[String](util.Arrays.asList("sell")))
        val subscription = createSubscriptionObject(new Document(params))
        subscription.subscriptionId.get mustBe "abcdef123456abcdef123456"
        subscription.subscriber mustBe "viktors@gmail.com"
        subscription.priceRange.get.from.get mustBe 1
        subscription.priceRange.get.to.get mustBe 5
        subscription.sizeRange.get.from.get mustBe 2
        subscription.sizeRange.get.to.get mustBe 6
        subscription.floorRange.get.from.get mustBe 3
        subscription.floorRange.get.to.get mustBe 10
        subscription.cities.get must contain ("riga")
        subscription.cities.get must contain ("jurmala")
        subscription.districts.get must contain ("centre")
        subscription.districts.get must contain ("teika")
        subscription.actions.get must contain ("sell")
      }
      "all fields except subscriber are empty" in {
        val params = new java.util.HashMap[String, Object]()
        params.put("subscriber","viktors@gmail.com")
        val subscription = createSubscriptionObject(new Document(params))
        subscription.subscriptionId mustBe None
        subscription.subscriber mustBe "viktors@gmail.com"
        subscription.priceRange mustBe None
        subscription.priceRange mustBe None
        subscription.sizeRange mustBe None
        subscription.sizeRange mustBe None
        subscription.floorRange mustBe None
        subscription.floorRange mustBe None
        subscription.cities mustBe None
        subscription.districts mustBe None
        subscription.actions mustBe None
      }
      "some fields are empty" in {
        val params = new java.util.HashMap[String, Object]()
        params.put("subscriber","viktors@gmail.com")
        params.put("priceRange",createRangeDocument(None,Option(5)))
        params.put("sizeRange",createRangeDocument(Option(2),None))
        params.put("floorRange",createRangeDocument(None,Option(10)))
        params.put("cities",new util.ArrayList[String](util.Arrays.asList("riga","jurmala")))
        params.put("actions",new util.ArrayList[String](util.Arrays.asList("sell")))
        val subscription = createSubscriptionObject(new Document(params))
        subscription.subscriptionId mustBe None
        subscription.subscriber mustBe "viktors@gmail.com"
        subscription.priceRange.get.from mustBe None
        subscription.priceRange.get.to.get mustBe 5
        subscription.sizeRange.get.from.get mustBe 2
        subscription.sizeRange.get.to mustBe None
        subscription.floorRange.get.from mustBe None
        subscription.floorRange.get.to.get mustBe 10
        subscription.cities.get must contain ("riga")
        subscription.cities.get must contain ("jurmala")
        subscription.districts mustBe None
        subscription.actions.get must contain ("sell")
      }
    }
  }

  private def createRangeDocument(from:Option[Int],to:Option[Int]): Document = {
    val rangeDocument = new util.HashMap[String, Object]()
    if (from != None) {
      rangeDocument.put("from", java.lang.Integer
        .valueOf(from.get))
    }
    if (to != None) {
      rangeDocument.put("to", java.lang.Integer
        .valueOf(to.get))
    }
    new Document(rangeDocument)
  }

}
