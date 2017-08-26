package actors.functions

import model.b2b.FlatRequestQuery
import model.b2c.{Flat, SellerContactDetails}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
  * Created by oginskis on 05/06/2017.
  */
object ContentExtractingFunctions {

  val SsLvBaseUrl = "ss.lv.base.url"
  val PathToFlats = "ss.lv.path.to.flats"

  def extractFlatContactDetails(addUrl: String, wsClient: WSClient, configuration: Configuration) = {

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

    val request = wsClient.url(addUrl)
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

  def extractFlatsFromPage(flatRequestQuery: FlatRequestQuery, page: Int,
                                   wsClient: WSClient, configuration: Configuration): List[Flat] = {

    def mapToFlats(entry: JsoupElement): Flat = {
      val attr: List[JsoupElement] = entry.select(".msga2-o").toList
      val link: String = entry.select(".msg2 .d1 .am").head.attr("href")
      val rawFloor = attr(3).text.replace("\\", "/")
      Flat(
        status = Option("New"),
        address = Option(attr(0).text.replace("\\", "/")),
        rooms = Option(Try(attr(1).text.trim.replace("\\", "/").toInt).getOrElse(-1)),
        size = Option(attr(2).text.trim.toInt),
        floor = Option(rawFloor.substring(0, rawFloor.lastIndexOf("/")).toDouble.toInt),
        maxFloors = Option(rawFloor.substring(rawFloor.lastIndexOf("/") + 1).toInt),
        price = Option(attr(6).text.replace(",", "").replace(" €", "").trim.toInt),
        link = Option(link))
    }
    def filterOutRubbish(entry: JsoupElement): Boolean = {
      val attr: List[JsoupElement] = entry.select(".msga2-o").toList
      attr(0).text.length > 3 &&
        attr(0).text != "-" &&
        attr(6).text.contains("€") &&
        !attr(6).text.contains("mai") &&
        !attr(6).text.contains("mēn") &&
        attr(6).text.length > 3
    }

    val url = (configuration.get[String](SsLvBaseUrl) + configuration.get[String](PathToFlats)
      + "/" + flatRequestQuery.city.get + "/" + flatRequestQuery.district.get + "/" + flatRequestQuery.action.get
      + "/page" + page + ".html")
    Logger.info(s"Extracting from $url")
    val request = wsClient.url(url)
      .withRequestTimeout(10.second)
      .withFollowRedirects(false)
    val future: Future[String] = request.get().map(
      response => response.body
    )
    val document = new JsoupBrowser().parseString(Await.result(future, 10.second))
    val entries = document.body.select("[id^=\"tr_\"]")
    Logger.info(s"Number of flats on $url: ${entries.size}")
    if (entries.isEmpty) {
      List[Flat]()
    } else {
      entries.init.toList
        .filter(filterOutRubbish)
        .map(mapToFlats)
        .map(flat => {
          Flat(address = flat.address,
            rooms = flat.rooms,
            size = flat.size,
            floor = flat.floor,
            maxFloors = flat.maxFloors,
            price = flat.price,
            link = flat.link,
            city = flatRequestQuery.city,
            district = flatRequestQuery.district,
            action = flatRequestQuery.action)
        })
    }
  }

}
