package repo

import java.time.Instant
import java.util
import javax.inject.{Inject, Singleton}

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.{or, _}
import configuration.MongoConnection
import model.b2c.{Flat, Subscription, SubscriptionActivationRequest}
import org.bson.Document
import repo.helpers.SubscriptionRepoHelper._
import model.CommonProps._

import scala.collection.JavaConverters._
import scala.services.EmailSendingService
import scala.util.{Failure, Success, Try}

@Singleton
class SubscriptionRepo @Inject()(connection: MongoConnection, emailSendingService: EmailSendingService) {

  private val subscriptionCollection = connection.getCollection(SubscriptionRepo.CollName)

  def createSubscription(subscription: Subscription): Boolean = {
    subscription.subscriber match {
      case subscriber if subscriber.matches(EmailRegexp) => {
        val activationToken = java.util.UUID.randomUUID.toString.replace("-","")
        val savedSubscription = Subscription(
          subscriber = subscription.subscriber,
          priceRange = subscription.priceRange,
          sizeRange = subscription.sizeRange,
          floorRange = subscription.floorRange,
          buildingTypes = subscription.buildingTypes,
          cities = subscription.cities,
          districts = subscription.districts,
          actions = subscription.actions,
          lastUpdatedDateTime = Option(Instant.now.getEpochSecond),
          enabled = Option(false)
        )
        Try(subscriptionCollection.insertOne(createSubscriptionDocument(SubscriptionActivationRequest(Option(activationToken),
          savedSubscription)))) match {
          case Success(_) => {
              emailSendingService.sendSubscriptionActivationEmail(SubscriptionActivationRequest(Option(activationToken),
                savedSubscription))
              true
          }
          case Failure(ex) => throw new RuntimeException(s"Failed to create a subscription. Error: ${ex.getMessage}")
        }
      }
      case _ => {
        false
      }
    }
  }

  def enableSubscription(activationToken: String): Boolean = {
    subscriptionAction(activationToken,true)
  }

  def disableSubscription(activationToken: String): Boolean = {
    subscriptionAction(activationToken,false)
  }

  def getSubscriptionToken(subscriptionId: String): Option[String] = {
    val params = createFindSubscriptionByIdDocumentQueryDoc(Option(subscriptionId))
    val documents = subscriptionCollection.find(params)
    if (documents.iterator.hasNext) {
      val document = documents.iterator.next
      Option(document.getString("activationToken"))
    } else {
      None
    }
  }

  private def subscriptionAction(activationToken: String, enable: Boolean) = {
    activationToken match {
      case activationToken if activationToken.matches(HexadecimalRegexp32) => {
        val params = createFindSubscriptionByIdActivationTokenQueryDoc(activationToken,!enable)
        val updParams = new java.util.HashMap[String, Object]()
        updParams.put("enabled", java.lang.Boolean.valueOf(enable))
        val result = subscriptionCollection.findOneAndUpdate(params, new Document("$set", new Document(updParams)))
        if (result == null){
          false
        } else true
      }
      case _ => false
    }
  }

  def getSubscriptionById(subscriptionId: String): Option[Subscription] = {
    subscriptionId match {
      case id if id.matches(HexadecimalRegexp) => {
        val documents = subscriptionCollection.find(createFindSubscriptionByIdDocumentQueryDoc(Option(id)))
        if (documents.iterator.hasNext) {
          Some(createSubscriptionObject(documents.iterator.next))
        } else {
          None
        }
      }
      case _ => None
    }
  }

  def deleteSubscriptionById(subscriptionId: String): Long = {
    subscriptionId match {
      case id if id.matches(HexadecimalRegexp) => {
        val result = subscriptionCollection.deleteOne(createFindSubscriptionByIdDocumentQueryDoc(Option(id)))
        result.getDeletedCount
      }
      case _ => {
        throw new IllegalArgumentException("Cannot delete subscription. Subscription id is empty or not valid")
      }
    }
  }

  def findAllSubscriptionsForEmail(email: String): List[Subscription] = {
    email match {
      case email if email.matches(EmailRegexp) => {
        val params = new util.HashMap[String, Object]()
        params.put("subscriber", email)
        params.put("enabled",java.lang.Boolean.valueOf(true))
        val documents = subscriptionCollection.find(new Document(params)).asScala.toList
        documents.map(document => createSubscriptionObject(document))
      }
      case _ => List[Subscription]()
    }
  }

  def findAllSubscribersForFlat(flat: Flat): List[Subscription] = {
    val query = and(
      or(lte("priceRange.from", flat.price.get), Filters.eq("priceRange.from", null)),
      or(gte("priceRange.to", flat.price.get), Filters.eq("priceRange.to", null)),
      or(lte("sizeRange.from", flat.size.get), Filters.eq("sizeRange.from", null)),
      or(gte("sizeRange.to", flat.size.get), Filters.eq("sizeRange.to", null)),
      or(lte("floorRange.from", flat.floor.get), Filters.eq("floorRange.from", null)),
      or(gte("floorRange.to", flat.floor.get), Filters.eq("floorRange.to", null)),
      Filters.eq("itemType", "subscription"),
      Filters.eq("enabled",java.lang.Boolean.valueOf(true))
    )
    def deduplicate(incomingDocs: List[Document],filteredDocs: List[Document],
                    seen: Seq[String]): List[Document] ={
      if (incomingDocs.size > 0) {
        val document = incomingDocs.head
        val subscriber = document.getString("subscriber")
        if (seen.contains(subscriber)) {
          deduplicate(incomingDocs.tail, filteredDocs, seen)
        } else {
          deduplicate(incomingDocs.tail, document :: filteredDocs, seen :+ subscriber)
        }
      } else {
        filteredDocs
      }
    }
    def contains(properties:Document,field: String, value: Option[String]):Boolean = {
      val property = properties.get(field)
      if (property == null) {
        true
      } else {
        if (property.asInstanceOf[java.util.List[String]].contains(value.getOrElse(""))){
          true
        } else {
          false
        }
      }
    }
    val documents:List[Document] = subscriptionCollection.find(query).asScala.toList
    val filtered = documents.filter(document => {
      val params = document.get("parameters")
      if (params == null){
        true
      } else {
        val props = params.asInstanceOf[Document]
        contains(props,"cities",flat.city) &&
        contains(props,"districts",flat.district) &&
        contains(props,"actions",flat.action)
      }
    })
    deduplicate(filtered,List[Document](),Seq[String]()).map(document => createSubscriptionObject(document))
  }
}

object SubscriptionRepo {
  val CollName = "flats"
}
