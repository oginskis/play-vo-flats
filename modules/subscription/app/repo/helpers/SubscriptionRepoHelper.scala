package repo.helpers

import java.util

import model.CommonProps
import model.b2c.{Range, Subscription, SubscriptionActivationRequest}
import org.bson.Document
import org.bson.types.ObjectId

import scala.collection.JavaConverters._
import scala.util.Try
import model.CommonProps._

object SubscriptionRepoHelper {

  def createSubscriptionDocument(subscriptionActivationRequest:SubscriptionActivationRequest): Document = {
    val document = createSubscriptionDocument(subscriptionActivationRequest.subscription)
    document.append("activationToken", subscriptionActivationRequest.token.getOrElse(EmptyResponse))
  }

  def createSubscriptionDocument(subscription: Subscription): Document = {
    val params = new java.util.HashMap[String, Object]()
    params.put("subscriber", subscription.subscriber)
    params.put("language",subscription.language)
    for (subscriptionId <- subscription.subscriptionId){
      params.put("_id",subscriptionId)
    }
    for (enabled <- subscription.enabled){
      params.put("enabled",java.lang.Boolean.valueOf(enabled))
    }
    for (priceRange <- subscription.priceRange) {
      params.put("priceRange",getRangeDocument(priceRange))
    }
    for (floorRange <- subscription.floorRange) {
      params.put("floorRange",getRangeDocument(floorRange))
    }
    for (sizeRange <- subscription.sizeRange) {
      params.put("sizeRange",getRangeDocument(sizeRange))
    }
    val listParameters = new util.HashMap[String,Object]()
    for (cities <- subscription.cities) {
      listParameters.put("cities",getListDocument(cities))
    }
    for (districts <- subscription.districts) {
      listParameters.put("districts",getListDocument(districts))
    }
    for (actions <- subscription.actions) {
      listParameters.put("actions",getListDocument(actions))
    }
    if (!listParameters.isEmpty){
      params.put("parameters",new Document(listParameters))
    }
    for (lastUpdatedDateTime <- subscription.lastUpdatedDateTime){
      params.put("lastUpdatedDateTime",java.lang.Long.valueOf(lastUpdatedDateTime))
    }
    params.put("itemType", "subscription")
    new Document(params)
  }

  def createSubscriptionActivationRequestQueryDoc(activationToken: String): Document = {
    val params = new java.util.HashMap[String, Object]()
    params.put("activationToken",activationToken)
    new Document(params)
  }

  def createFindSubscriptionByIdDocumentQueryDoc(subscription: Subscription): Document = {
    createFindSubscriptionByIdDocumentQueryDoc(subscription.subscriptionId)
  }

  def createFindSubscriptionByIdDocumentQueryDoc(id: Option[String]): Document = {
    val document = createFindSubscriptionBy(id,"_id")
    document.append("enabled",java.lang.Boolean.valueOf(true))
  }

  def createFindSubscriptionByIdActivationTokenQueryDoc(token: String, subscriptionEnabled: Boolean): Document = {
    val document = createFindSubscriptionBy(Option(token),"activationToken")
    document.append("enabled",java.lang.Boolean.valueOf(subscriptionEnabled))
  }

  def createFindSubscriptionBy(id: Option[String], fieldName: String): Document = {
    val params = new java.util.HashMap[String, Object]()
    id match {
      case Some(id) => {
        if (fieldName == "_id") {
          params.put(fieldName, new ObjectId(id))
        }
        else {
          params.put(fieldName, id)
        }
      }
      case None => {
        throw new IllegalArgumentException("id is empty")
      }
    }
    params.put("itemType", "subscription")
    new Document(params)
  }

  private def getListDocument(array: Array[String]): util.ArrayList[String] = {
    val list = new java.util.ArrayList[String]()
    array.foreach(city => list.add(city))
    list
  }

  private def getRangeDocument(range: Range): Document = {
    val rangeDocument = new util.HashMap[String, Object]()
    for (from <- range.from) {
      rangeDocument.put("from", java.lang.Integer.valueOf(from))
    }
    for (to <- range.to) {
      rangeDocument.put("to", java.lang.Integer.valueOf(to))
    }
    new Document(rangeDocument)
  }

  def createSubscriptionObject(document: Document): Subscription = {
    new Subscription(
      subscriptionId = Try(
        {
          val id = document.get("_id").toString
          if (id.matches(CommonProps.EmailRegexp)){
            Option(document.get("id").toString)
          } else {
            Option(id)
          }
        }
      ).getOrElse(None),
      subscriber = document.get("subscriber").toString,
      priceRange = getRangeObject(document.get("priceRange")),
      sizeRange = getRangeObject(document.get("sizeRange")),
      floorRange = getRangeObject(document.get("floorRange")),
      cities = getListObject(document.get("parameters"),"cities"),
      districts = getListObject(document.get("parameters"),"districts"),
      actions = getListObject(document.get("parameters"),"actions"),
      enabled = Option(document.getBoolean("enabled")),
      lastUpdatedDateTime = Option(document.get("lastUpdatedDateTime").toString.toLong),
      language = Try(document.get("language").toString).getOrElse("en")
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
