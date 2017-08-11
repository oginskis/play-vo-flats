package repo.helpers

import java.util

import model.b2c.{Range, Subscription}
import org.bson.Document

import scala.collection.JavaConverters._
import scala.util.Try

object SubscriptionRepoHelper {

  def createSubscriptionDocument(subscription: Subscription): Document = {
    val params = new java.util.HashMap[String, Object]()
    params.put("subscriber",subscription.subscriber)
    if (subscription.priceRange != None) {
      params.put("priceRange",getRangeDocument(subscription.priceRange.get))
    }
    if (subscription.floorRange != None) {
      params.put("floorRange",getRangeDocument(subscription.floorRange.get))
    }
    if (subscription.sizeRange != None) {
      params.put("sizeRange",getRangeDocument(subscription.sizeRange.get))
    }
    if (subscription.cities != None){
      params.put("cities",getListDocument(subscription.cities.get))
    }
    if (subscription.districts != None){
      params.put("districts",getListDocument(subscription.districts.get))
    }
    if (subscription.actions != None){
      params.put("actions",getListDocument(subscription.actions.get))
    }
    params.put("itemType", "subscription")
    new Document(params)
  }

  def createSubscriptionObject(document: Document): Subscription = {
    new Subscription(
      subscriptionId = Option(document.get("_id").toString),
      subscriber = document.get("subscriber").toString,
      priceRange = getRangeObject(document.get("priceRange")),
      sizeRange = getRangeObject(document.get("sizeRange")),
      floorRange = getRangeObject(document.get("floorRange")),
      cities = getListObject(document.get("cities")),
      districts = getListObject(document.get("districts")),
      actions = getListObject(document.get("actions"))
    )
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
    if (range.from != None){
      rangeDocument.put("from",java.lang.Integer
        .valueOf(range.from.get))
    }
    if (range.to != None){
      rangeDocument.put("to",java.lang.Integer
        .valueOf(range.to.get))
    }
    new Document(rangeDocument)
  }

  private def getRangeObject(rangeDocument: Object): Option[Range] = {
    if (rangeDocument != null && rangeDocument.isInstanceOf[Document]) {
      val priceRangeDoc = rangeDocument.asInstanceOf[Document]
      Option(new Range(Try(Option(priceRangeDoc.get("from").toString.toInt)).getOrElse(None)
        ,Try(Option(priceRangeDoc.get("to").toString.toInt)).getOrElse(None)))
    }
    else {
      None
    }
  }

  private def getListObject(listObject: Object): Option[Array[String]] = {
    if (listObject != null) {
      val list = listObject.asInstanceOf[java.util.ArrayList[String]].asScala
      Option(list.toArray)
    }
    else {
      None
    }
  }

}
