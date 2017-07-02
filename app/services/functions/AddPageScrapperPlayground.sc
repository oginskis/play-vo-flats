import com.typesafe.config.ConfigFactory
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.asynchttpclient.AsyncHttpClientConfig
import play.api._
import play.api.libs.ws.ahc.{AhcConfigBuilder, AhcWSClient, AhcWSClientConfig}

import scala.concurrent.{Await, Future}

import scala.concurrent.duration._

val configuration = Configuration.reference ++ Configuration(ConfigFactory.parseString(
  """
    |ws.followRedirects = true
  """.stripMargin))


val config = new AhcWSClientConfig()
val builder = new AhcConfigBuilder(config)
val logging = new AsyncHttpClientConfig.AdditionalChannelInitializer() {
  override def initChannel(channel: io.netty.channel.Channel): Unit = {
    channel.pipeline.addFirst("log", new io.netty.handler.logging.LoggingHandler("debug"))
  }
}
val ahcBuilder = builder.configure()
ahcBuilder.setHttpAdditionalChannelInitializer(logging)
val ahcConfig = ahcBuilder.build()
val wsClient = AhcWSClient(ahcConfig)

val request = wsClient.url("https://www.ss.lv/msg/lv/real-estate/flats/riga/centre/bfcmeh.html")
  .withRequestTimeout(5.seconds)
  .withFollowRedirects(false)
val future: Future[String] = request.get().map(
  response => response.body
)
val document = new JsoupBrowser().parseString(Await.result(future, 10.second))
val contacts = document.body.select(".ads_contacts_name")