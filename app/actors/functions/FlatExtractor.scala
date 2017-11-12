package actors.functions

import model.b2b.FlatRequestQuery
import model.b2c.Flat
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import FlatExtractor._

import scala.concurrent.{Await, Future}
import scala.util.Try

class FlatExtractor(wsClient: WSClient, configuration: Configuration)
  extends ((FlatRequestQuery,Int) => List[Flat]) {

  override def apply(flatRequestQuery: FlatRequestQuery, page: Int): List[Flat] = {
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
        buildingType = Option(attr(4).text.trim),
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
            buildingType = flat.buildingType,
            link = flat.link,
            city = flatRequestQuery.city,
            district = flatRequestQuery.district,
            action = flatRequestQuery.action)
        })
    }
  }
}

object FlatExtractor {
  val SsLvBaseUrl = "ss.lv.base.url"
  val PathToFlats = "ss.lv.path.to.flats"
}
