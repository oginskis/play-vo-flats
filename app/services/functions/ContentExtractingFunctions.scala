package services.functions

import model.b2b.FlatRequestQuery
import model.b2c.Flat
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by oginskis on 05/06/2017.
  */
object ContentExtractingFunctions {

  val SS_LV_BASE = "ss.lv.base.url"

  def sslv(flatRequestQuery: FlatRequestQuery, wsClient: WSClient, configuration: Configuration): List[Flat] = {
    def extractFlats(page: Int): List[Flat] = {
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
      val rawFlatList = document.body.select("[id^=\"tr_\"]")
      if (rawFlatList.isEmpty) {
        return List[Flat]()
      }
      rawFlatList.init.toList
        .filter(entry => {
          val attr: List[JsoupElement] = entry.select(".msga2-o").toList
          attr(0).text.length > 3 &&
            attr(0).text != "-" &&
            attr(6).text.contains("€") &&
            !attr(6).text.contains("mai") &&
            !attr(6).text.contains("mēn")&&
            attr(6).text.length > 3
        }
        )
        .map(entry => {
          val attr: List[JsoupElement] = entry.select(".msga2-o").toList
          val link: String = entry.select(".msg2 .d1 .am").head.attr("href")
          new Flat(Option(attr(0).text.replace("\\", "/")),
            Option(attr(1).text.trim.replace("\\", "/")),
            Option(attr(2).text.trim.toInt),
            Option(attr(3).text.replace("\\", "/")),
            Option(attr(6).text.replace(",", "").replace(" €", "").trim.toInt),
            Option(link),
            flatRequestQuery.city,
            flatRequestQuery.district,
            flatRequestQuery.action)
        }) ::: extractFlats(page + 1)
    }
    extractFlats(1)
  }

}
