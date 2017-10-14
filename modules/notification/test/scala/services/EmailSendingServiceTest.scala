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
    "be sent (LV)" in {
      val emailSendingService = getGuiceContext.injector.instanceOf[EmailSendingService]
      emailSendingService.sendFlatNotificationEmail(FlatNotification(
        Option(createFlat),Option(createSubscription("lv")),Option("aabbcc")))
      val messages = fakeSmtp.getReceivedEmails.asScala
      messages.headOption match {
        case Some(message) => {
          message.getHeaderValue("From") mustBe "no-reply@adscraper.lv"
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
          message.getBody.contains("E-pasta adrese") mustBe true
          message.getBody.contains("Pils=C4=93tas") mustBe true
          message.getBody.contains("Rajoni") mustBe true
          message.getBody.contains("Darb=C4=ABbas") mustBe true
          message.getBody.contains("Cenas diapazons") mustBe true
          message.getBody.contains("Plat=C4=ABbas diapazons") mustBe true
          message.getBody.contains("St=C4=81vu diapazons") mustBe true
          message.getBody.contains("Atrakst=C4=ABties") mustBe true
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
          message.getBody.contains("Tu esi sa=C5=86=C4=93mis =C5=A1o e-pastu, " +
            "jo es=i pierakst=C4=ABjies sekojo=C5=A1am filtram:") mustBe true
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
    "be sent (LV)" in {
      val emailSendingService = getGuiceContext.injector.instanceOf[EmailSendingService]
      val uuid = java.util.UUID.randomUUID.toString.replace("-","")
      emailSendingService
        .sendSubscriptionActivationEmail(SubscriptionActivationRequest(Option(uuid),createSubscription("lv")))
      val messages = fakeSmtp.getReceivedEmails.asScala
      messages.headOption match {
        case Some(message) => {
          checkCommonFieldsLv(message,uuid)
          message.getBody.contains("Pils=C4=93tas") mustBe true
          message.getBody.contains("Rajoni") mustBe true
          message.getBody.contains("Darb=C4=ABbas") mustBe true
          message.getBody.contains("Cenas diapazons") mustBe true
          message.getBody.contains("Plat=C4=ABbas diapazons") mustBe true
          message.getBody.contains("St=C4=81vu diapazons") mustBe true
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
    "be sent, empty subscription (LV)" in {
      val emailSendingService = getGuiceContext.injector.instanceOf[EmailSendingService]
      val uuid = java.util.UUID.randomUUID.toString.replace("-","")
      emailSendingService
        .sendSubscriptionActivationEmail(SubscriptionActivationRequest(Option(uuid),createEmptySubscription("lv")))
      val messages = fakeSmtp.getReceivedEmails.asScala
      messages.headOption match {
        case Some(message) => {
          checkCommonFieldsLv(message,uuid)
          message.getBody.contains("UZMAN=C4=AABU! J=C5=ABs neizv=C4=93l=C4=93j=C4==81ties filtr=C4=93=C5=A1anas " +
            "krit=C4=93rijus. J=C5=ABs sa=C5=86emsiet pazi==C5=86ojumus par VISIEM jauniem " +
            "sludin=C4=81jumiem un eso=C5=A1o sludin=C4==81jumu cenu izmai=C5=86=C4=81m! ") mustBe true
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

  private def checkCommonFieldsLv(message: SmtpMessage,uuid: String) = {
    message.getHeaderValue("From") mustBe "no-reply@adscraper.lv"
    message.getHeaderValue("To") mustBe "viktors.oginskis@gmail.com"
    message.getBody.contains("Filtra abon=C4=93=C5=A1anas apstiprin=C4=81=C5==A1ana") mustBe true
    message.getBody.contains("K=C4=81ds tikko ir re=C4=A3istr=C4=93jis") mustBe true
    message.getBody.contains("viktors.oginskis@gmail.com") mustBe true
    message.getBody.contains("=C4=93pastu pazi=C5=86o=jumu sa=C5=86em=C5=A1anai par") mustBe true
    message.getBody.contains("jauniem dz=C4=ABvok=C4=BCu sludin=C4=81jumiem= un eso=C5=A1o sludin=C4=81jumu cenu izmai=C5=86=C4=81m port=C4=81l=C4=81") mustBe true
    message.getBody.contains("ss.com") mustBe true
    message.getBody.contains("Pazi=C5=86ojumi tiks s=C5=ABt=C4=ABti par sludin=C4==81jumiem, kas atbilst sekojo=C5=A1am filtram:") mustBe true
    message.getBody.contains("Lai aktiviz=C4=93tu filtru, nospiediet zem=C4=81k e=so=C5=A1o pogu") mustBe true
    message.getBody.contains("Ja j=C5=ABst nevarat nospiest pogu, l=C5=ABdzu ieko=p=C4=93jiet sekojo=C5=A1u saiti j=C5=ABsu p=C4=81rlukprogramm=C4=81:") mustBe true
    message.getBody.contains(s"subscription/enable/$uuid") mustBe true
    message.getBody.contains("Ja j=C5=ABs vairs nev=C4=93laties abon=C4=93t filtr=u, l=C5=ABdzu ignor=C4=93jiet =C5=A1o =C4=93pastu") mustBe true
  }

  private def createSubscription(lang: String) = {
    val subscription = Subscription(
      subscriber = "viktors.oginskis@gmail.com",
      priceRange = Option(Range(Option(70000), None)),
      floorRange = Option(Range(None, Option(5))),
      sizeRange = Option(Range(Option(40), Option(90))),
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
