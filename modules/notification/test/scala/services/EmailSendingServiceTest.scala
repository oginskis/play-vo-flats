package scala.services

import java.time.Instant

import com.dumbster.smtp.SimpleSmtpServer
import model.b2c.{Flat, SellerContactDetails, Subscription}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.PlaySpec

import scala.testhelpers.TestApplicationContextHelper._
import scala.collection.JavaConverters._

class EmailSendingServiceTest extends PlaySpec with BeforeAndAfterAll with BeforeAndAfter {

  val fakeSmtp = SimpleSmtpServer.start(2525)

  "Notification email" should {
    "be sent (EN)" in {
      val emailSendingService = getGuiceContext.injector.instanceOf[EmailSendingService]
      val flat = Flat(
        status = Option("New"),
        address = Option("Caka 95"),
        rooms = Option(2),
        size = Option(75),
        floor = Option(2),
        maxFloors = Option(5),
        price = Option(80000),
        link = Option("ss.lv"),
        firstSeenAt = Option(Instant.now.getEpochSecond),
        lastSeenAt = Option(Instant.now.getEpochSecond),
        Option("riga"),
        Option("teika"),
        Option("sell"),
        expired = Option("false"),
        contactDetails = Option(SellerContactDetails(
          Option(List[String]("29556271","29556272")),
          Option("www.seller.lv"),
          Option("Company")
        ))
      )
      val subscription = Subscription(
        subscriber = "viktors.oginskis@gmail.com",
        priceRange = None,
        floorRange = None,
        sizeRange = None,
        cities = None,
        districts = None,
        actions = None,
        lastUpdatedDateTime = Option(Instant.now.getEpochSecond)
      )
      emailSendingService.sendEmail(flat,subscription)
      val messages = fakeSmtp.getReceivedEmails.asScala
      messages.headOption match {
        case Some(message) => {
          message.getHeaderValue("From") mustBe "no-reply@addscraper.lv"
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
        }
        case None => fail("list does not contain message")
      }
      fakeSmtp.reset
    }
    "be sent (LV)" in {
      val emailSendingService = getGuiceContext.injector.instanceOf[EmailSendingService]
      val flat = Flat(
        status = Option("New"),
        address = Option("Caka 95"),
        rooms = Option(2),
        size = Option(75),
        floor = Option(2),
        maxFloors = Option(5),
        price = Option(80000),
        link = Option("ss.lv"),
        firstSeenAt = Option(Instant.now.getEpochSecond),
        lastSeenAt = Option(Instant.now.getEpochSecond),
        Option("riga"),
        Option("teika"),
        Option("sell"),
        expired = Option("false"),
        contactDetails = Option(SellerContactDetails(
          Option(List[String]("29556271","29556272")),
          Option("www.seller.lv"),
          Option("Company")
        ))
      )
      val subscription = Subscription(
        subscriber = "viktors.oginskis@gmail.com",
        priceRange = None,
        floorRange = None,
        sizeRange = None,
        cities = None,
        districts = None,
        actions = None,
        lastUpdatedDateTime = Option(Instant.now.getEpochSecond),
        language = "lv"
      )
      emailSendingService.sendEmail(flat,subscription)
      val messages = fakeSmtp.getReceivedEmails.asScala
      messages.headOption match {
        case Some(message) => {
          message.getHeaderValue("From") mustBe "no-reply@addscraper.lv"
          message.getHeaderValue("To") mustBe "viktors.oginskis@gmail.com"
          message.getHeaderValue("Subject") mustBe "Caka 95, teika, riga, 80000 EUR"
          message.getBody.contains("Jauns dz=C4=ABvoklis tika pievienots") mustBe true
          message.getBody.contains("Adrese") mustBe true
          message.getBody.contains("Pils=C4=93ta, rajons") mustBe true
          message.getBody.contains("Plat=C4=ABba") mustBe true
          message.getBody.contains("St=C4=81vs") mustBe true
          message.getBody.contains("Istabu skaits") mustBe true
          message.getBody.contains("Cena") mustBe true
          message.getBody.contains("Links") mustBe true
          message.getBody.contains("Kas public=C4=93ja?") mustBe true
          message.getBody.contains("T=C4=81lru=C5=86i") mustBe true
          message.getBody.contains("Uz=C5=86=C4=93mums") mustBe true
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
        }
        case None => fail("list does not contain message")
      }
      fakeSmtp.reset
    }
  }
}
