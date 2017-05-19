package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import repo.FlatRepo

/**
  * Created by oginskis on 16/05/2017.
  */
@Singleton
class FlatController @Inject() (flatRepo: FlatRepo) extends Controller {

  def show (flatId: String) = Action {
    Ok(Json.toJson(flatRepo.getFlatById(flatId)))
  }
  

}
