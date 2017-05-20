package controllers

import com.google.inject.{Inject, Singleton}
import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import repo.FlatSearchRepo

/**
  * Created by oginskis on 18/05/2017.
  */
@Singleton
class SearchController @Inject () (searchRepo: FlatSearchRepo) extends Controller {

  def search(searchKey: String) = Action {
    Ok(Json.toJson(searchRepo.searchFlats(searchKey)))
  }
}
