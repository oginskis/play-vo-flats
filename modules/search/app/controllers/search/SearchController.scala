package controllers.search

import javax.inject.{Inject, Singleton}

import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import repo.FlatSearchRepo

/**
  * Created by oginskis on 18/05/2017.
  */
@Singleton
class SearchController @Inject()(cc:ControllerComponents,searchRepo: FlatSearchRepo)
  extends AbstractController(cc) {

  def search(searchKey: String) = Action {
    Ok(Json.toJson(searchRepo.searchFlats(searchKey)))
  }
}
