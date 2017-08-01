package repo

import java.util
import javax.inject.{Inject, Singleton}

import configuration.MongoConnection
import model.b2c.Subscription
import org.bson.Document
import org.bson.types.ObjectId
import repo.helpers.SubscriptionRepoHelper._

@Singleton
class SubscriptionRepo @Inject()(connection: MongoConnection)  {

  private val subscriptionCollection = connection.getCollection(SubscriptionRepo.CollName)

  def createSubscription(subscription: Subscription) = {
    subscriptionCollection.insertOne(createSubscriptionDocument(subscription))
  }

  def getSubscriptionById(subscriptionId: String): Option[Subscription] = {
    val params = new util.HashMap[String, Object]()
    params.put("_id", new ObjectId(subscriptionId))
    val documents = subscriptionCollection.find(new Document(params))
    if (documents.iterator.hasNext){
      Some(createSubscriptionObject(documents.iterator.next))
    }
    None
  }
}

object SubscriptionRepo {
  val CollName = "flats"
}
