package model.b2c

import play.api.libs.json.{Json, Writes}

case class Error(
                name: String,
                description: String
                ) {
}

object Error {

  def mustBeHexadecimal(fieldName:String): Error = {
    new Error("Invalid Input",
      fieldName + " must be valid hexadecimal string")
  }

  implicit val rangeWrites = new Writes[Error] {
    override def writes(error: Error)= {
      Json.obj(
        "errorName" -> error.name,
        "errorDescription" -> error.description
      )
    }
  }
}
