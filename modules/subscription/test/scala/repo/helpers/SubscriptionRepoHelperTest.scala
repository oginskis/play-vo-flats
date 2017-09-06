package scala.repo.helpers

import java.time.Instant
import java.util

import model.b2c.Subscription
import org.bson.Document
import org.scalatestplus.play.PlaySpec
import repo.helpers.SubscriptionRepoHelper._

class SubscriptionRepoHelperTest extends PlaySpec {

  var currentDateTimeEpoch:Long = Instant.now.getEpochSecond

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
          actions = Option(Array[String]("sell")),
          lastUpdatedDateTime = Option(currentDateTimeEpoch)
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
        val params = doc.get("parameters").asInstanceOf[Document]
        params.get("cities").asInstanceOf[util.ArrayList[String]].contains("riga") mustBe true
        params.get("cities").asInstanceOf[util.ArrayList[String]].contains("jurmala") mustBe true
        params.get("districts").asInstanceOf[util.ArrayList[String]].contains("centrs") mustBe true
        params.get("actions").asInstanceOf[util.ArrayList[String]].contains("sell") mustBe true
        doc.getLong("lastUpdatedDateTime") mustBe currentDateTimeEpoch
        doc.getBoolean("enabled") mustBe false
        doc.getString("language") mustBe "en"
      }
      "all fields except subscriber and enabled, and language fields are empty" in {
        val subscription = new Subscription(
          subscriber = "viktors@gmail.com",
          priceRange = None,
          sizeRange = None,
          floorRange = None,
          cities = None,
          districts = None,
          actions = None,
          enabled = Option(true),
          lastUpdatedDateTime = Option(currentDateTimeEpoch),
          language = "lv"
        )
        val doc = createSubscriptionDocument(subscription)
        doc.get("subscriber").toString mustBe "viktors@gmail.com"
        doc.get("priceRange") mustBe null
        doc.get("sizeRange") mustBe null
        doc.get("floorRange") mustBe null
        doc.get("cities") mustBe null
        doc.get("districts") mustBe null
        doc.get("actions") mustBe null
        doc.get("enabled") mustBe true
        doc.get("language") mustBe "lv"
      }
      "some fields are empty" in {
        val subscription = new Subscription(
          subscriber = "viktors@gmail.com",
          priceRange = Option(new model.b2c.Range(Option(1), None)),
          sizeRange = Option(new model.b2c.Range(None, Option(5))),
          floorRange = Option(new model.b2c.Range(Option(2), None)),
          cities = None,
          districts = Option(Array[String]("centrs")),
          actions = None,
          enabled = Option(false),
          lastUpdatedDateTime = Option(currentDateTimeEpoch),
          language = "en"
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
        val params = doc.get("parameters").asInstanceOf[Document]
        params.get("cities") mustBe null
        params.get("districts").asInstanceOf[util.ArrayList[String]].contains("centrs") mustBe true
        params.get("actions") mustBe null
        doc.getLong("lastUpdatedDateTime") mustBe currentDateTimeEpoch
        doc.get("enabled") mustBe false
        doc.get("language") mustBe "en"
      }
    }
  }

  "Subscription domain object" should {
    "be created out of subscription document object" when {
      "all fields are present" in {
        val params = new java.util.HashMap[String, Object]()
        params.put("lastUpdatedDateTime",java.lang.Long.valueOf(currentDateTimeEpoch))
        params.put("_id","abcdef123456abcdef123456")
        params.put("enabled",java.lang.Boolean.valueOf(true))
        params.put("subscriber","viktors@gmail.com")
        params.put("priceRange",createRangeDocument(Option(1),Option(5)))
        params.put("sizeRange",createRangeDocument(Option(2),Option(6)))
        params.put("floorRange",createRangeDocument(Option(3),Option(10)))
        params.put("language","en")
        val parameters = new util.HashMap[String,Object]()
        parameters.put("cities",new util.ArrayList[String](util.Arrays.asList("riga","jurmala")))
        parameters.put("districts",new util.ArrayList[String](util.Arrays.asList("centre","teika")))
        parameters.put("actions",new util.ArrayList[String](util.Arrays.asList("sell")))
        params.put("parameters",new Document(parameters))
        val subscription = createSubscriptionObject(new Document(params))
        subscription.subscriptionId.get mustBe "abcdef123456abcdef123456"
        subscription.subscriber mustBe "viktors@gmail.com"
        subscription.language mustBe "en"
        subscription.priceRange match {
          case Some(range) => {
            range.from mustBe Some(1)
            range.to mustBe Some(5)
          }
          case None => fail("priceRange must be present")
        }
        subscription.sizeRange match {
          case Some(range) => {
            range.from mustBe Some(2)
            range.to mustBe Some(6)
          }
          case None => fail("sizeRange must be present")
        }
        subscription.floorRange match {
          case Some(range) => {
            range.from mustBe Some(3)
            range.to mustBe Some(10)
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
        subscription.lastUpdatedDateTime mustBe Some(currentDateTimeEpoch)
        subscription.enabled mustBe Some(true)
      }
      "all fields except subscriber are empty" in {
        val params = new java.util.HashMap[String, Object]()
        params.put("subscriber","viktors@gmail.com")
        params.put("lastUpdatedDateTime",java.lang.Long.valueOf(currentDateTimeEpoch))
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
        subscription.lastUpdatedDateTime mustBe Some(currentDateTimeEpoch)
        subscription.enabled mustBe Some(false)
        subscription.language mustBe "en"
      }
      "some fields are empty" in {
        val params = new java.util.HashMap[String, Object]()
        params.put("subscriber","viktors@gmail.com")
        params.put("priceRange",createRangeDocument(None,Option(5)))
        params.put("sizeRange",createRangeDocument(Option(2),None))
        params.put("floorRange",createRangeDocument(None,Option(10)))
        params.put("lastUpdatedDateTime",java.lang.Long.valueOf(currentDateTimeEpoch))
        params.put("enabled",java.lang.Boolean.valueOf(true))
        params.put("language","lv")
        val parameters = new util.HashMap[String,Object]()
        parameters.put("cities",new util.ArrayList[String](util.Arrays.asList("riga","jurmala")))
        parameters.put("actions",new util.ArrayList[String](util.Arrays.asList("sell")))
        params.put("parameters",new Document(parameters))
        val subscription = createSubscriptionObject(new Document(params))
        subscription.subscriptionId mustBe None
        subscription.subscriber mustBe "viktors@gmail.com"
        subscription.language mustBe "lv"
        subscription.priceRange match {
          case Some(range) => {
            range.from mustBe None
            range.to mustBe Some(5)
          }
          case None => fail("priceRange must be present")
        }
        subscription.sizeRange match {
          case Some(range) => {
            range.from mustBe Some(2)
            range.to mustBe None
          }
          case None => fail("sizeRange must be present")
        }
        subscription.floorRange match {
          case Some(range) => {
            range.from mustBe None
            range.to mustBe Some(10)
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
        subscription.districts mustBe None
        subscription.actions match {
          case Some(list) => {
            list must contain("sell")
          }
          case None => fail("Actions list must not be empty")
        }
        subscription.lastUpdatedDateTime mustBe Some(currentDateTimeEpoch)
        subscription.enabled mustBe Some(true)
      }
    }
  }

  private def createRangeDocument(from:Option[Int],to:Option[Int]): Document = {
    val rangeDocument = new util.HashMap[String, Object]()
    from match {
      case Some(value) => rangeDocument.put("from", java.lang.Integer.valueOf(value))
      case None => {}
    }
    to match {
      case Some(value) => rangeDocument.put("to", java.lang.Integer.valueOf(value))
      case None => {}
    }
    new Document(rangeDocument)
  }

}
