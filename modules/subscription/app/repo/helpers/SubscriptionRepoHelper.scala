package repo.helpers

import java.util

import model.b2c.Subscription
import org.bson.Document

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
      params.put("priceRange",paramsPriceRange)
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
      params.put("floorRange",paramsFloorRange)
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
      params.put("sizeRange",paramsSizeRange)
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

}
