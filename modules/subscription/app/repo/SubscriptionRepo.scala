package repo

import java.time.Instant
import java.util
import javax.inject.{Inject, Singleton}

import com.mongodb.client.model.{Aggregates, Filters}
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
          cities = subscription.cities,
          districts = subscription.districts,
          actions = subscription.actions,
          lastUpdatedDateTime = Option(Instant.now.getEpochSecond),
          enabled = Option(false)
        )
        Try(subscriptionCollection.insertOne(createSubscriptionDocument(SubscriptionActivationRequest(activationToken,
          savedSubscription)))) match {
          case Success(_) => {
              emailSendingService.sendSubscriptionActivationEmail(SubscriptionActivationRequest(activationToken,
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
    activationToken match {
      case activationToken if activationToken.matches(HexadecimalRegexp32) => {
        val params = createFindSubscriptionByIdActivationTokenQueryDoc(activationToken)
        val updParams = new java.util.HashMap[String, Object]()
        updParams.put("enabled", java.lang.Boolean.valueOf(true))
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
      or(Filters.eq("parameters.cities", flat.city.get), Filters.eq("parameters.cities", null)),
      or(Filters.eq("parameters.districts", flat.district.get), Filters.eq("parameters.districts", null)),
      or(Filters.eq("parameters.actions", flat.action.get), Filters.eq("parameters.actions", null)),
      Filters.eq("itemType", "subscription"),
      Filters.eq("enabled",java.lang.Boolean.valueOf(true))
    )
    val documents = subscriptionCollection.aggregate(util.Arrays.asList(
      Aggregates.`match`(query),
      new Document("$sort",new Document("subscriber",1)),
      new Document("$group",new Document("_id","$subscriber")
        .append("id",new Document("$first","$_id"))
        .append("subscriber",new Document("$first","$subscriber"))
        .append("itemType",new Document("$first","$itemType"))
        .append("priceRange",new Document("$first","$priceRange"))
        .append("sizeRange",new Document("$first","$sizeRange"))
        .append("floorRange",new Document("$first","$floorRange"))
        .append("parameters",new Document("$first","$parameters"))
        .append("language",new Document("$first","$language"))
      )
      )
    ).asScala.toList
    documents.map(document => createSubscriptionObject(document))
  }
}

object SubscriptionRepo {
  val CollName = "flats"
}
