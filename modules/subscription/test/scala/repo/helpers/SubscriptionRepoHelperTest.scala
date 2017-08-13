package scala.repo.helpers

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

}
