package configuration

import javax.inject.{Inject, Singleton}

import com.mongodb.client.MongoCollection
import com.mongodb.{MongoClient, MongoClientURI}
import org.bson.Document
import play.api.Configuration

@Singleton
class DefaultMongoConnection @Inject()(configuration: Configuration) extends MongoConnection{

  private val mongoDb = new MongoClient(new MongoClientURI("mongodb://" +
    configuration.get[String](DefaultMongoConnection.MongoDbUser) + ":" +
    configuration.get[String](DefaultMongoConnection.MongoDbPassword) + "@" +
    configuration.get[String](DefaultMongoConnection.MongoDbHost) + ":" +
    configuration.get[Int](DefaultMongoConnection.MongoDbPort) + "/?ssl=" +
    configuration.get[Boolean](DefaultMongoConnection.MongoDbSsl) +
    configuration.get[String](DefaultMongoConnection.MongoDbAdditionalProps)))
    .getDatabase(configuration.get[String](DefaultMongoConnection.MongoDbDb))

  override def getCollection(collectionName:String): MongoCollection[Document] ={
    mongoDb.getCollection(collectionName)
  }
}

object DefaultMongoConnection {
  val MongoDbUser = "mongodb.user"
  val MongoDbDb = "mongodb.db"
  val MongoDbPort = "mongodb.port"
  val MongoDbPassword = "mongodb.password"
  val MongoDbHost = "mongodb.host"
  val MongoDbSsl = "mongodb.ssl"
  val CollName = "flats"
  val MongoDbAdditionalProps = "mongodb.additionalProps"
}
