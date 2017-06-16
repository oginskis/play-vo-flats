package services.functions

import model.b2b.FlatRequestQuery
import model.b2c.Flat
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import play.api.{Logger, Configuration}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by oginskis on 05/06/2017.
  */
object ContentExtractingFunctions {

  val SS_LV_BASE = "ss.lv.base.url"

  def sslv(flatRequestQuery: FlatRequestQuery, wsClient: WSClient, configuration: Configuration): List[Flat] = {

    def extract(page: Int): List[Flat] = {
      def mappingFunc(entry: JsoupElement): Flat = {
        val attr: List[JsoupElement] = entry.select(".msga2-o").toList
        val link: String = entry.select(".msg2 .d1 .am").head.attr("href")
        new Flat(Option(attr(0).text.replace("\\", "/")),
          Option(attr(1).text.trim.replace("\\", "/")),
          Option(attr(2).text.trim.toInt),
          Option(attr(3).text.replace("\\", "/")),
          Option(attr(6).text.replace(",", "").replace(" €", "").trim.toInt),
          Option(link))
      }
      def filteringFunc(entry: JsoupElement): Boolean = {
        val attr: List[JsoupElement] = entry.select(".msga2-o").toList
        attr(0).text.length > 3 &&
          attr(0).text != "-" &&
          attr(6).text.contains("€") &&
          !attr(6).text.contains("mai") &&
          !attr(6).text.contains("mēn")&&
          attr(6).text.length > 3
      }
      val url = (configuration.underlying.getString(ContentExtractingFunctions.SS_LV_BASE)
        + "/" + flatRequestQuery.city.get + "/" + flatRequestQuery.district.get + "/" + flatRequestQuery.action.get
        + "/page" + page + ".html")
      Logger.info(s"Processing $url")
      val request = wsClient.url(url)
        .withRequestTimeout(5.second)
        .withFollowRedirects(false)
      val future: Future[String] = request.get().map(
        response => response.body
      )
      val document = new JsoupBrowser().parseString(Await.result(future, 10.second))
      val entries = document.body.select("[id^=\"tr_\"]")
      if (entries.isEmpty) {
        return List[Flat]()
      }
      entries.init.toList
        .filter(filteringFunc)
        .map(mappingFunc)
        .map(flat => new Flat(flat.address,
          flat.rooms,
          flat.size,
          flat.floor,
          flat.price,
          flat.link,
          flatRequestQuery.city,
          flatRequestQuery.district,
          flatRequestQuery.action)) ::: extract(page + 1)
    }
    extract(1)
  }


}
