package scala.services

import java.time.Instant

import com.dumbster.smtp.{SimpleSmtpServer, SmtpMessage}
import model.b2c._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec

import scala.testhelpers.TestApplicationContextHelper._
import scala.collection.JavaConverters._

class EmailSendingServiceTest extends PlaySpec with BeforeAndAfterAll with BeforeAndAfter {

  val fakeSmtp = SimpleSmtpServer.start(2525)

  override def afterAll = {
    fakeSmtp.stop()
  }

  "Notification email" should {
    "be sent (EN)" in {
      val emailSendingService = getGuiceContext.injector.instanceOf[EmailSendingService]
      emailSendingService.sendFlatNotificationEmail(FlatNotification(
        Option(createFlat),Option(createSubscription("en")),Option("aabbcc")))
      val messages = fakeSmtp.getReceivedEmails.asScala
      messages.headOption match {
        case Some(message) => {
          message.getHeaderValue("From") mustBe "no-reply@adscraper.lv"
          message.getHeaderValue("To") mustBe "viktors.oginskis@gmail.com"
          message.getHeaderValue("Subject") mustBe "Caka 95, teika, riga, 80000 EUR"
          message.getBody.contains("New flat has been added") mustBe true
          message.getBody.contains("Address") mustBe true
          message.getBody.contains("City, District") mustBe true
          message.getBody.contains("Size") mustBe true
          message.getBody.contains("Number of floors") mustBe true
          message.getBody.contains("Number of rooms") mustBe true
          message.getBody.contains("Price") mustBe true
          message.getBody.contains("Link") mustBe true
          message.getBody.contains("Who published?") mustBe true
          message.getBody.contains("Phone number(s)") mustBe true
          message.getBody.contains("Company") mustBe true
          message.getBody.contains("WWW") mustBe true
          message.getBody.contains("www.adscraper.lv") mustBe true
          message.getBody.contains("WWW") mustBe true
          message.getBody.contains("Caka 95") mustBe true
          message.getBody.contains("riga, teika") mustBe true
          message.getBody.contains("75  m2") mustBe true
          message.getBody.contains("2/5") mustBe true
          message.getBody.contains("80000 EUR") mustBe true
          message.getBody.contains("https://www.ss.comss.lv") mustBe true
          message.getBody.contains("29556271, 29556272") mustBe true
          message.getBody.contains("Company") mustBe true
          message.getBody.contains("Email address") mustBe true
          message.getBody.contains("Cities") mustBe true
          message.getBody.contains("Districts") mustBe true
          message.getBody.contains("Actions") mustBe true
          message.getBody.contains("Price range") mustBe true
          message.getBody.contains("Size range") mustBe true
          message.getBody.contains("Floor range") mustBe true
          message.getBody.contains("Unsubscribe") mustBe true
          message.getBody.contains("www.adscraper.lv") mustBe true
          message.getBody.contains("viktors.oginskis@gmail.com") mustBe true
          message.getBody.contains("riga") mustBe true
          message.getBody.contains("teika, centrs") mustBe true
          message.getBody.contains("sell") mustBe true
          message.getBody.contains("70000") mustBe true
          message.getBody.contains("EUR") mustBe true
          message.getBody.contains("40") mustBe true
          message.getBody.contains("90") mustBe true
          message.getBody.contains("m2") mustBe true
          message.getBody.contains("5") mustBe true
          message.getBody.contains("...") mustBe true
          message.getBody.contains("You received this email because you had " +
            "been subscribed to the following filter:") mustBe true
          message.getBody.contains("subscription/disable/aabbcc") mustBe true

        }
        case None => fail("list does not contain message")
      }
      fakeSmtp.reset
    }
  }
  "Activation email" should {
    "be sent (EN)" in {
      val emailSendingService = getGuiceContext.injector.instanceOf[EmailSendingService]
      val uuid = java.util.UUID.randomUUID.toString.replace("-","")
      emailSendingService
        .sendSubscriptionActivationEmail(SubscriptionActivationRequest(Option(uuid),createSubscription("en")))
      val messages = fakeSmtp.getReceivedEmails.asScala
      messages.headOption match {
        case Some(message) => {
          checkCommonFieldsEn(message,uuid)
          message.getBody.contains("Cities") mustBe true
          message.getBody.contains("Districts") mustBe true
          message.getBody.contains("Actions") mustBe true
          message.getBody.contains("Price range") mustBe true
          message.getBody.contains("Size range") mustBe true
          message.getBody.contains("Floor range") mustBe true
          message.getBody.contains("riga") mustBe true
          message.getBody.contains("teika, centrs") mustBe true
          message.getBody.contains("sell") mustBe true
          message.getBody.contains("70000 EUR") mustBe true
          message.getBody.contains("40 m2") mustBe true
          message.getBody.contains("90 m2") mustBe true
        }
        case None => fail("list does not contain message")
      }
      fakeSmtp.reset
    }
    "be sent, empty subscription (EN)" in {
      val emailSendingService = getGuiceContext.injector.instanceOf[EmailSendingService]
      val uuid = java.util.UUID.randomUUID.toString.replace("-","")
      emailSendingService
        .sendSubscriptionActivationEmail(SubscriptionActivationRequest(Option(uuid),createEmptySubscription("en")))
      val messages = fakeSmtp.getReceivedEmails.asScala
      messages.headOption match {
        case Some(message) => {
          checkCommonFieldsEn(message,uuid)
          message.getBody.contains("WARNING! No filter criteria has been set. " +
            "You will receive notifications about ALL new apartment advertisements " +
            "and price changes of existing advertisements!") mustBe true
        }
        case None => fail("list does not contain message")
      }

      fakeSmtp.reset
    }
  }

  private def checkCommonFieldsEn(message: SmtpMessage,uuid: String) = {
    message.getHeaderValue("From") mustBe "no-reply@adscraper.lv"
    message.getHeaderValue("To") mustBe "viktors.oginskis@gmail.com"
    message.getHeaderValue("Subject") mustBe "Activate your subscription"
    message.getBody.contains("Activate your subscription") mustBe true
    message.getBody.contains("Somebody just subscribed") mustBe true
    message.getBody.contains("viktors.oginskis@gmail.com") mustBe true
    message.getBody.contains("to receive email notifications regarding") mustBe true
    message.getBody.contains("new apartment advertisements and price changes of existing advertisements posted on") mustBe true
    message.getBody.contains("ss.com") mustBe true
    message.getBody.contains("Notifications will be sent for new apartment advertisements that meet the following filter:") mustBe true
    message.getBody.contains("In order to activate the filter click the button below") mustBe true
    message.getBody.contains("Activate") mustBe true
    message.getBody.contains("If you can&#x27;t click the button above, then copy and submit the following link to your browser:") mustBe true
    message.getBody.contains(s"subscription/enable/$uuid") mustBe true
    message.getBody.contains("If you don&#x27;t want to subscribe to the filter anymore, then please ignore this email") mustBe true
  }

  private def createSubscription(lang: String) = {
    val subscription = Subscription(
      subscriber = "viktors.oginskis@gmail.com",
      priceRange = Option(Range(Option(70000), None)),
      floorRange = Option(Range(None, Option(5))),
      sizeRange = Option(Range(Option(40), Option(90))),
      buildingTypes = Option(List("Specpr.").toArray),
      cities = Option(List("riga").toArray),
      districts = Option(Array("teika", "centrs")),
      actions = Option(Array("sell")),
      language = lang,
      lastUpdatedDateTime = Option(Instant.now.getEpochSecond)
    )
    subscription
  }

  private def createEmptySubscription(lang: String) = {
    val subscription = Subscription(
      subscriber = "viktors.oginskis@gmail.com",
      priceRange = None,
      floorRange = None,
      sizeRange = None,
      cities = None,
      buildingTypes = None,
      districts = None,
      actions = None,
      language = lang,
      lastUpdatedDateTime = Option(Instant.now.getEpochSecond)
    )
    subscription
  }

  private def createFlat = {
    val flat = Flat(
      status = Option("New"),
      address = Option("Caka 95"),
      rooms = Option(2),
      size = Option(75),
      floor = Option(2),
      maxFloors = Option(5),
      price = Option(80000),
      buildingType = Option("Specpr."),
      link = Option("ss.lv"),
      firstSeenAt = Option(Instant.now.getEpochSecond),
      lastSeenAt = Option(Instant.now.getEpochSecond),
      Option("riga"),
      Option("teika"),
      Option("sell"),
      expired = Option("false"),
      contactDetails = Option(SellerContactDetails(
        Option(List[String]("29556271", "29556272")),
        Option("www.seller.lv"),
        Option("Company")
      ))
    )
    flat
  }
}
