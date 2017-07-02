package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import repo.FlatRepo

/**
  * Created by oginskis on 16/05/2017.
  */
@Singleton
class FlatController @Inject()(cc: ControllerComponents, flatRepo: FlatRepo) extends AbstractController(cc) {

  def show(flatId: String) = Action {
    Ok(Json.toJson(flatRepo.getFlatById(flatId)))
  }


}
