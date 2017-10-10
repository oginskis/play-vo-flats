package model.b2c

import play.api.libs.json.{Json, Writes}

case class Success(status: String,
                   description: String) {

}

object Success {

  implicit val successWrites = new Writes[Success] {
    override def writes(success: Success)= {
      Json.obj(
        "status" -> success.status,
        "description" -> success.description
      )
    }
  }
}
