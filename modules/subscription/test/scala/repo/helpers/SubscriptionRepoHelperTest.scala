package scala.repo.helpers

import model.b2c.Subscription
import org.bson.Document
import org.scalatest.{FlatSpec, Matchers}
import repo.helpers.SubscriptionRepoHelper._

class SubscriptionRepoHelperTest extends FlatSpec with Matchers{

  "Subscription document object" should "correspond to Subscription domain object" in {
      val subscription = new Subscription(
        subscriber = "viktors@gmail.com",
        priceRange = Option(new model.b2c.Range(Option(1),Option(3))),
        sizeRange = Option(new model.b2c.Range(Option(2),Option(5))),
        floorRange = Option(new model.b2c.Range(Option(2),Option(6))),
        cities = Option(Array[String]("riga","jurmala")),
        districts = Option(Array[String]("centrs")),
        actions = Option(Array[String]("sell"))
      )
    val doc:Document = createSubscriptionDocument(subscription)
      doc.get("subscriber").toString should be ("viktors@gmail.com")
      val priceRange = doc.get("priceRange").asInstanceOf[Document]
      priceRange.get("from") should be (1)
      priceRange.get("to") should be (3)
      val sizeRange = doc.get("sizeRange").asInstanceOf[Document]
      sizeRange.get("from") should be (2)
      sizeRange.get("to") should be (5)
      val floorRange = doc.get("floorRange").asInstanceOf[Document]
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

  it should "be created out of Subscription domain object even " +
    "if all fields except subscriber field are empty" in {
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
    doc.get("subscriber").toString should be ("viktors@gmail.com")
    doc.get("priceRange") should be (null)
    doc.get("sizeRange") should be (null)
    doc.get("floorRange") should be (null)
    doc.get("cities") should be (null)
    doc.get("districts") should be (null)
    doc.get("actions") should be (null)
  }

  it should "be created out of Subscription domain object even " +
    "if some fields are empty" in {
    val subscription = new Subscription(
      subscriber = "viktors@gmail.com",
      priceRange = Option(new model.b2c.Range(Option(1),None)),
      sizeRange = Option(new model.b2c.Range(None,Option(5))),
      floorRange = Option(new model.b2c.Range(Option(2),None)),
      cities = None,
      districts = Option(Array[String]("centrs")),
      actions = None
    )
    val doc:Document = createSubscriptionDocument(subscription)
    doc.get("subscriber").toString should be ("viktors@gmail.com")
    val priceRange = doc.get("priceRange").asInstanceOf[Document]
    priceRange.get("from") should be (1)
    priceRange.get("to") should be (null)
    val sizeRange = doc.get("sizeRange").asInstanceOf[Document]
    sizeRange.get("from") should be (null)
    sizeRange.get("to") should be (5)
    val floorRange = doc.get("floorRange").asInstanceOf[Document]
    floorRange.get("from") should be (2)
    floorRange.get("to") should be (null)
    doc.get("cities") should be (null)
    val districts = doc.get("districts").asInstanceOf[java.util.ArrayList[String]]
    districts.contains("centrs") should be (true)
    doc.get("actions") should be (null)
  }

}
