package repo.helpers

import java.util
import model.b2c.Range

import model.b2c.Subscription
import org.bson.Document

import scala.collection.JavaConverters._

import scala.util.Try

object SubscriptionRepoHelper {

  def createSubscriptionDocument(subscription: Subscription): Document = {
    val params = new java.util.HashMap[String, Object]()
    params.put("subscriber",subscription.subscriber.get)
    if (subscription.priceRange != None) {
      val paramsPriceRange = new util.HashMap[String, Object]()
      if (subscription.priceRange.get.from != None){
        paramsPriceRange.put("from",java.lang.Integer
          .valueOf(subscription.priceRange.get.from.get))
      }
      if (subscription.priceRange.get.to != None){
        paramsPriceRange.put("to",java.lang.Integer.
          valueOf(subscription.priceRange.get.to.get))
      }
      params.put("priceRange",new Document(paramsPriceRange))
    }
    if (subscription.floorRange != None) {
      val paramsFloorRange = new util.HashMap[String, Object]()
      if (subscription.floorRange.get.from != None){
        paramsFloorRange.put("from",java.lang.Integer
          .valueOf(subscription.floorRange.get.from.get))
      }
      if (subscription.floorRange.get.to != None){
        paramsFloorRange.put("to",java.lang.Integer
          .valueOf(subscription.floorRange.get.to.get))
      }
      params.put("floorRange",new Document(paramsFloorRange))
    }
    if (subscription.sizeRange != None) {
      val paramsSizeRange = new util.HashMap[String, Object]()
      if (subscription.sizeRange.get.from != None){
        paramsSizeRange.put("from",java.lang.Integer
          .valueOf(subscription.sizeRange.get.from.get))
      }
      if (subscription.sizeRange.get.to != None){
        paramsSizeRange.put("to",java.lang.Integer
          .valueOf(subscription.sizeRange.get.to.get))
      }
      params.put("sizeRange",new Document(paramsSizeRange))
    }
    if (subscription.cities != None){
      val paramsCities = new java.util.ArrayList[String]()
      subscription.cities.get.foreach(city => {
        paramsCities.add(city)
      }
      )
      params.put("cities",paramsCities)
    }
    if (subscription.districts != None){
      val paramsDistricts = new util.ArrayList[String]()
      subscription.districts.get.foreach(district => {
        paramsDistricts.add(district)
      }
      )
      params.put("districts",paramsDistricts)
    }
    if (subscription.actions != None){
      val paramsActions = new util.ArrayList[String]()
      subscription.actions.get.foreach(action => {
        paramsActions.add(action)
      }
      )
      params.put("actions",paramsActions)
    }
    params.put("itemType", "subscription")
    new Document(params)
  }

  def createSubscriptionObject(document: Document): Subscription = {
    new Subscription(
      subscriptionId = Option(document.get("_id").toString),
      subscriber = Option(document.get("subscriber").toString),
      priceRange = getRangeObject(document.get("priceRange")),
      sizeRange = getRangeObject(document.get("sizeRange")),
      floorRange = getRangeObject(document.get("floorRange")),
      cities = getListObject(document.get("cities")),
      districts = getListObject(document.get("districts")),
      actions = getListObject(document.get("actions"))
    )
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
    if (listObject != null && listObject.isInstanceOf[java.util.ArrayList[String]]) {
      val list = listObject.asInstanceOf[java.util.ArrayList[String]].asScala
      Option(list.toArray)
    }
    else {
      None
    }
  }

}
