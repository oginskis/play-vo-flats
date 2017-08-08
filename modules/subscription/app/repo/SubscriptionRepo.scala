package repo

import java.util
import javax.inject.{Inject, Singleton}

import configuration.MongoConnection
import model.b2c.Subscription
import org.bson.Document
import org.bson.types.ObjectId
import play.shaded.ahc.io.netty.util.internal.StringUtil
import repo.helpers.SubscriptionRepoHelper._

import scala.collection.JavaConverters._

@Singleton
class SubscriptionRepo @Inject()(connection: MongoConnection)  {

  private val subscriptionCollection = connection.getCollection(SubscriptionRepo.CollName)

  def createSubscription(subscription: Subscription) = {
    subscriptionCollection.insertOne(createSubscriptionDocument(subscription))
  }

  def getSubscriptionById(subscriptionId: String): Option[Subscription] = {
    if (StringUtil.isNullOrEmpty(subscriptionId)){
      return None
    }
    val params = new util.HashMap[String, Object]()
    params.put("_id", new ObjectId(subscriptionId))
    params.put("itemType","subscription")
    val documents = subscriptionCollection.find(new Document(params))
    if (documents.iterator.hasNext){
      return Some(createSubscriptionObject(documents.iterator.next))
    }
    None
  }

  def deleteSubscriptionById(subscriptionId: String): Long = {
    if (StringUtil.isNullOrEmpty(subscriptionId)){
      throw new IllegalArgumentException("Cannot delete subscription. SubscriptionId was not set")
    }
    val params = new util.HashMap[String, Object]()
    params.put("_id", new ObjectId(subscriptionId))
    val result = subscriptionCollection.deleteOne(new Document(params))
    result.getDeletedCount
  }

  def findAllSubscriptionsForEmail(email: String): Option[List[Subscription]] = {
    if (StringUtil.isNullOrEmpty(email)){
      return None
    }
    val params = new util.HashMap[String, Object]()
    params.put("subscriber", email)
    val documents = subscriptionCollection.find(new Document(params)).asScala.toList
    return Option(documents.map(document => createSubscriptionObject(document)))
  }

}

object SubscriptionRepo {
  val CollName = "flats"
}
