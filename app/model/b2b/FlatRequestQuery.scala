package model.b2b

/**
  * Created by oginskis on 26/05/2017.
  */
case class FlatRequestQuery(
                             val city: Option[String],
                             val district: Option[String],
                             val action: Option[String]
                           ) {
  override def toString: String = {
    "city: " + city.getOrElse(FlatRequestQuery.EMPTY_PROP) + ", " +
    "district: " + district.getOrElse(FlatRequestQuery.EMPTY_PROP) + ", " +
    "action: " + action.getOrElse(FlatRequestQuery.EMPTY_PROP)
  }
}

object FlatRequestQuery {
  val EMPTY_PROP = "Empty"
}