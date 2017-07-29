package scala.repo.helpers

import model.b2c.Subscription
import org.bson.Document
import org.scalatest.{FlatSpec, Matchers}
import repo.helpers.SubscriptionRepoHelper._

class SubscriptionRepoHelperTest extends FlatSpec with Matchers{

  "MongoDB Subscription document object" should "correspond to Subscription domain object" in {
      val subscription = new Subscription(
        subscriber = Option("viktors@gmail.com"),
        priceRange = Option(new model.b2c.Range(Option(1),Option(3))),
        sizeRange = Option(new model.b2c.Range(Option(2),Option(5))),
        floorRange = Option(new model.b2c.Range(Option(2),Option(6))),
        cities = Option(Array[String]("riga","jurmala")),
        districts = Option(Array[String]("centrs")),
        actions = Option(Array[String]("sell"))
      )
      val doc:Document = createSubscriptionDocument(subscription)
      doc.get("subscriber").toString should be ("viktors@gmail.com")
      val priceRange = doc.get("priceRange").asInstanceOf[java.util.HashMap[String,Object]]
      priceRange.get("from") should be (1)
      priceRange.get("to") should be (3)
      val sizeRange = doc.get("sizeRange").asInstanceOf[java.util.HashMap[String,Object]]
      sizeRange.get("from") should be (2)
      sizeRange.get("to") should be (5)
      val floorRange = doc.get("floorRange").asInstanceOf[java.util.HashMap[String,Object]]
      floorRange.get("from") should be (2)
      floorRange.get("to") should be (6)
      val cities = doc.get("cities").asInstanceOf[java.util.ArrayList[String]]
      cities.contains("riga") should be (true)
      cities.contains("jurmala") should be (true)
      val districts = doc.get("districts").asInstanceOf[java.util.ArrayList[String]]
      districts.contains("centrs") should be (true)
      val actions = doc.get("actions").asInstanceOf[java.util.ArrayList[String]]
      actions.contains("sell") should be (true)
    }
}
