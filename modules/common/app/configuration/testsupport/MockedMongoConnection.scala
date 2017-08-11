package configuration.testsupport

import com.mongodb.MongoClient
import com.mongodb.client.{MongoCollection, MongoDatabase}
import configuration.MongoConnection
import org.bson.Document
import MockedMongoConnection._

class MockedMongoConnection extends MongoConnection{

  val mongoDb: MongoDatabase = {
    val mongo = new MongoClient(InMemoryMongoHost, InMemoryMongoPort)
    val db = mongo.getDatabase("test")
    db.createCollection("flats")
    db
  }

  override def getCollection(collectionName: String): MongoCollection[Document] = {
    mongoDb.getCollection(collectionName)
  }
}

object MockedMongoConnection {
  val InMemoryMongoHost = "localhost"
  val InMemoryMongoPort = 12345
}
