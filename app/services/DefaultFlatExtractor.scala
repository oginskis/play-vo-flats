package services

import com.google.inject.{Inject, Singleton}
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
  * Created by oginskis on 21/05/2017.
  */
@Singleton
class DefaultFlatExtractor @Inject()(wsClient: WSClient, configuration: Configuration)
  extends FlatExtractor {

  override def extractFlats(flatRequestQuery: FlatRequestQuery): List[Flat] = {
    def extractFlats(page: Int): List[Flat] = {
      val url = (configuration.underlying.getString(DefaultFlatExtractor.SS_LV_BASE)
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

object DefaultFlatExtractor {
  val SS_LV_BASE = "ss.lv.base.url"
}