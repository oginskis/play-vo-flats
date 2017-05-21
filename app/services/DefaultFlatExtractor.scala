package services

import com.google.inject.{Inject, Singleton}
import model.Flat
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by oginskis on 21/05/2017.
  */
@Singleton
class DefaultFlatExtractor @Inject()(wsClient: WSClient, configuration: Configuration) extends FlatExtractor {

  override def extractFlats(): List[Flat] = {
      def extractFlats(page: Int) : List[Flat] = {
          val request = wsClient.url(configuration.underlying.getString(DefaultFlatExtractor.SS_LV_BASE)+
            "/riga/centre/sell/page"
            + page + ".html")
            .withRequestTimeout(5.second)
            .withFollowRedirects(false)
          val future: Future[WSResponse] = request.get()
          val response = Await.result(future,10.second)
          val document = new JsoupBrowser().parseString(response.body)
          val rawFlatList = document.body.select("[id^=\"tr_\"]")
          if (rawFlatList.isEmpty){
            return List[Flat]()
          }
          rawFlatList.init.toList.map(
            entry => {
              val attr: List[JsoupElement] = entry.select(".msga2-o").toList
              val link: String = entry.select(".msg2 .d1 .am").head.attr("href")
              new Flat(Option(attr(0).text.replace("\\", "/")),
                Option(attr(1).text.trim.replace("\\", "/")),
                Option(attr(2).text.trim.toInt),
                Option(attr(3).text.replace("\\", "/")),
                Option(attr(6).text.replace(",","").replace(" €","").trim.toInt),
                Option(link))
            }) ::: extractFlats(page + 1)
      }
      extractFlats(1)
    }
}

object DefaultFlatExtractor {
  val SS_LV_BASE ="ss.lv.base.url"
}