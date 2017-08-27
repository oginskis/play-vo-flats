package repo.helpers

import java.util
import model.b2c.{Range, Subscription}
import org.bson.Document
import scala.collection.JavaConverters._
import scala.util.Try

object SubscriptionRepoHelper {

  def createSubscriptionDocument(subscription: Subscription): Document = {
    val params = new java.util.HashMap[String, Object]()
    params.put("subscriber", subscription.subscriber)
    subscription.subscriptionId match {
      case Some(value) => params.put("_id",value)
      case None =>
    }
    subscription.enabled match {
      case Some(value) => params.put("enabled",java.lang.Boolean.valueOf(value))
      case None =>
    }
    subscription.priceRange match {
      case Some(value) => params.put("priceRange",getRangeDocument(value))
      case None =>
    }
    subscription.floorRange match {
      case Some(value) => params.put("floorRange",getRangeDocument(value))
      case None =>
    }
    subscription.sizeRange match {
      case Some(value) => params.put("sizeRange",getRangeDocument(value))
      case None =>
    }
    val listParameters = new util.HashMap[String,Object]()
    subscription.cities match {
      case Some(value) => listParameters.put("cities",getListDocument(value))
      case None =>
    }
    subscription.districts match {
      case Some(value) => listParameters.put("districts",getListDocument(value))
      case None =>
    }
    subscription.actions match {
      case Some(value) => listParameters.put("actions",getListDocument(value))
      case None =>
    }
    if (!listParameters.isEmpty){
      params.put("parameters",new Document(listParameters))
    }
    subscription.lastUpdatedDateTime match {
      case Some(value) => params.put("lastUpdatedDateTime",java.lang.Long.valueOf(value))
      case None =>
    }
    params.put("itemType", "subscription")
    new Document(params)
  }

  private def getListDocument(array: Array[String]): util.ArrayList[String] = {
    val list = new java.util.ArrayList[String]()
    array.foreach(city => {
        list.add(city)
      }
    )
    list
  }

  private def getRangeDocument(range: Range): Document = {
    val rangeDocument = new util.HashMap[String, Object]()
    if (range.from != None) {
      rangeDocument.put("from", java.lang.Integer
        .valueOf(range.from.get))
    }
    if (range.to != None) {
      rangeDocument.put("to", java.lang.Integer
        .valueOf(range.to.get))
    }
    new Document(rangeDocument)
  }

  def createSubscriptionObject(document: Document): Subscription = {
    new Subscription(
      subscriptionId = Try(Option(document.get("_id").toString)).getOrElse(None),
      subscriber = document.get("subscriber").toString,
      priceRange = getRangeObject(document.get("priceRange")),
      sizeRange = getRangeObject(document.get("sizeRange")),
      floorRange = getRangeObject(document.get("floorRange")),
      cities = getListObject(document.get("parameters"),"cities"),
      districts = getListObject(document.get("parameters"),"districts"),
      actions = getListObject(document.get("parameters"),"actions"),
      enabled = Option(document.getBoolean("enabled")),
      lastUpdatedDateTime = Option(document.getLong("lastUpdatedDateTime"))
    )
  }

  private def getRangeObject(rangeDocument: Object): Option[Range] = {
    if (rangeDocument != null && rangeDocument.isInstanceOf[Document]) {
      val priceRangeDoc = rangeDocument.asInstanceOf[Document]
      Option(new Range(Try(Option(priceRangeDoc.get("from").toString.toInt)).getOrElse(None)
        , Try(Option(priceRangeDoc.get("to").toString.toInt)).getOrElse(None)))
    }
    else {
      None
    }
  }

  private def getListObject(listObject: Object,parameterName: String): Option[Array[String]] = {
    if (listObject != null) {
      val doc = listObject.asInstanceOf[Document]
      return Try(Option(doc.get(parameterName).asInstanceOf[util.ArrayList[String]].asScala.toArray)).getOrElse(None)
    }
    else {
      None
    }
  }

}
