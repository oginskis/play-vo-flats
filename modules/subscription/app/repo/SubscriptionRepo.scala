package repo

import javax.inject.{Inject, Singleton}

import configuration.MongoConnection

@Singleton
class SubscriptionRepo @Inject()(connection: MongoConnection,configuration: play.api.Configuration)  {

  private val subscriptionCollection = connection.getCollection(SubscriptionRepo.CollName)

}

object SubscriptionRepo {
  val CollName = "flats"
}
