package repo

import java.util.Date

import com.google.inject.{Inject, Singleton}
import model.FlatSearchResult
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by oginskis on 18/05/2017.
  */
@Singleton
class FlatSearchRepo @Inject() (ws: WSClient, configuration: Configuration) {

  val AZURE_SEARCH_BASE_URL = "azureSearch.searchBaseUrl"
  val AZURE_SEARCH_API_KEY = "azureSearch.apiKey"

    def searchFlats(searchString: String): List[FlatSearchResult] = {
      val request = ws.url(configuration.getString(AZURE_SEARCH_BASE_URL).get+searchString+"*&" +
        "&$filter=lastSeenAtEpoch ge "+((new Date().getTime/1000)-600)+"&$top=10")
        .withHeaders("Accept" -> "application/json",
          "api-key"->configuration.getString(AZURE_SEARCH_API_KEY).get)
          .withRequestTimeout(5.second)
      val future: Future[List[FlatSearchResult]] = request.get().map {
        response =>
          (response.json \ "value" ).as[List[FlatSearchResult]]
      }
      Await.result(future,10.second)
    }

}
