package actors.functions

import model.b2c.SellerContactDetails
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}
import scala.util.Try
import FlatExtractor._

class ContactDetailsExtractor(wsClient: WSClient, configuration: Configuration)
  extends ((String) => Option[SellerContactDetails]){

  override def apply(urlOfAdvertisement: String): Option[SellerContactDetails] = {
    def extractPhoneNumbers(elements: List[JsoupElement]): Option[List[String]] = {
      Option(elements
        .filter(element => element.select(".ads_contacts_name").size > 0 &&
          element.select(".ads_contacts_name").head.text.contains("Tālrunis"))
        .map(element => {
          (element.select("[id^=\"phone_td_\"]").head.text.replace("(","").replace(")","").replace("-",""))
        }))
    }
    def extractWebPage(elements: List[JsoupElement]): Option[String] = {
      val wwwElement = Try(elements
        .filter(element => element.select(".ads_contacts_name").size > 0 &&
          element.select(".ads_contacts_name").head.text.contains("WWW")).head)
        .getOrElse({return None})
      val urlLink = Try(wwwElement.select("a").head
        .attr("href")).getOrElse(return None)
      val future: Future[String] = wsClient
        .url(configuration.get[String](SsLvBaseUrl)+urlLink)
        .withRequestTimeout(10.seconds).get().map(
        response => response.body
      )
      val document = new JsoupBrowser().parseString(Await.result(future, 10.second))
      Option(Try(document.head.select("script").head.toString
        .replace("JsoupElement(<script>document.location.href = \"","").replace("\";</script>)",""))
        .getOrElse(return None))
    }
    def extractCompany(elements: List[JsoupElement]): Option[String] = {
      Option(Try(elements
        .filter(element => element.select(".ads_contacts_name").size > 0 &&
          element.select(".ads_contacts_name").head.text.contains("Uzņēmums")).head
        .select(".ads_contacts").head.text)
        .getOrElse({return None}))
    }

    val request = wsClient.url(urlOfAdvertisement)
      .withRequestTimeout(10.seconds)
      .withFollowRedirects(false)
    val future: Future[String] = request.get().map(
      response => response.body
    )
    val document = new JsoupBrowser().parseString(Await.result(future, 10.second))
    val contacts = document.body.select(".contacts_table").select("tr")
    val contactList = contacts.toList
    Option(new SellerContactDetails(
      extractPhoneNumbers(contactList),
      extractWebPage(contactList),
      extractCompany(contactList)))
  }

}
