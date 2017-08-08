package configuration

import com.mongodb.client.MongoCollection
import org.bson.Document

trait MongoConnection {

  def getCollection(collectionName:String): MongoCollection[Document]

}
