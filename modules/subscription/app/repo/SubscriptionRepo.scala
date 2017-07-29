package repo

import javax.inject.{Inject, Singleton}

import configuration.MongoConnection
import model.b2c.Subscription

import repo.helpers.SubscriptionRepoHelper._

@Singleton
class SubscriptionRepo @Inject()(connection: MongoConnection)  {

  private val subscriptionCollection = connection.getCollection(SubscriptionRepo.CollName)

  def createSubscription(subscription: Subscription) = {
    subscriptionCollection.insertOne(createSubscriptionDocument(subscription))
  }
}

object SubscriptionRepo {
  val CollName = "flats"
}
