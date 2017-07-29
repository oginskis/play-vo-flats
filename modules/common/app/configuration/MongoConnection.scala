package configuration

import javax.inject.{Inject, Singleton}

import com.mongodb.client.MongoCollection
import com.mongodb.{MongoClient, MongoClientURI}
import org.bson.Document
import play.api.Configuration

@Singleton
class MongoConnection @Inject()(configuration: Configuration) {

  private val mongoDb = new MongoClient(new MongoClientURI("mongodb://" +
    configuration.get[String](MongoConnection.MongoDbUser) + ":" +
    configuration.get[String](MongoConnection.MongoDbPassword) + "@" +
    configuration.get[String](MongoConnection.MongoDbHost) + ":" +
    configuration.get[Int](MongoConnection.MongoDbPort) + "/?ssl=" +
    configuration.get[Boolean](MongoConnection.MongoDbSsl) +
    configuration.get[String](MongoConnection.MongoDbAdditionalProps)))
    .getDatabase(configuration.get[String](MongoConnection.MongoDbDb))

  def getCollection(collectionName:String): MongoCollection[Document] ={
    mongoDb.getCollection(collectionName)
  }
}

object MongoConnection {
  val MongoDbUser = "mongodb.user"
  val MongoDbDb = "mongodb.db"
  val MongoDbPort = "mongodb.port"
  val MongoDbPassword = "mongodb.password"
  val MongoDbHost = "mongodb.host"
  val MongoDbSsl = "mongodb.ssl"
  val CollName = "flats"
  val MongoDbAdditionalProps = "mongodb.additionalProps"
}
