package repo

import java.util
import java.util.Date
import javax.inject.{Inject, Singleton}

import com.mongodb.{MongoClient, MongoClientURI}
import model.Flat
import org.bson.Document
import org.bson.types.ObjectId
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
  * Created by oginskis on 30/12/2016.
  */
@Singleton
class FlatRepo @Inject() (configuration: play.api.Configuration) {

  val MONGODB_USER = "mongodb.user"
  val MONGODB_DB = "mongodb.db"
  val MONGODB_PORT = "mongodb.port"
  val MONGODB_PASSWORD = "mongodb.password"
  val MONGODB_HOST = "mongodb.host"
  val MONGODB_SSL = "mongodb.ssl"
  val COLL_NAME= "flats"

  val mongo = new MongoClient(new MongoClientURI("mongodb://"+configuration.underlying.getString(MONGODB_USER)+":"
    +configuration.underlying.getString(MONGODB_PASSWORD)+"@"+configuration.underlying.getString(MONGODB_HOST)
    +":"+configuration.underlying.getInt(MONGODB_PORT)+"/?ssl="+configuration.underlying.getBoolean(MONGODB_SSL)
    +"&maxIdleTimeMS=6000&minPoolSize=5"))
  val db = mongo.getDatabase(configuration.underlying.getString(MONGODB_DB))
  val flatsColl = db.getCollection(COLL_NAME)

  def getFlatById(flatId: String): Option[Flat] = {
    val params = new util.HashMap[String,Object]()
    params.put("_id",new ObjectId(flatId))
    val documents = flatsColl.find(new Document(params))
    if (documents.iterator().hasNext()){
      val doc = documents.iterator().next()
      Some(new Flat(
        Option(doc.get("address").toString),
        Option(doc.get("rooms").toString),
        Option(doc.get("size").toString.toInt),
        Option(doc.get("floor").toString),
        Option(doc.get("price").toString.toInt),
        Option(doc.get("link").toString),
        Option(doc.get("firstSeenAtEpoch").toString.toLong),
        Option(doc.get("lastSeenAtEpoch").toString.toLong)
      ))
    } else {
      None
    }
  }

  def addOrUpdateFlat(flat: Flat): Flat.Value = {
    def updateDocument(flat: Flat): org.bson.Document = {
      val params = new java.util.HashMap[String,Object]()
      params.put("address",flat.address.get)
      params.put("floor",flat.floor.get)
      params.put("link",flat.link.get)
      params.put("price",flat.price.get.toString)
      params.put("rooms",flat.rooms.get)
      params.put("size",flat.size.get.toString)
      params.put("lastSeenAtEpoch",(new Date().getTime/1000).toString)
      new org.bson.Document(params)
    }
    def createDocument(flat: Flat): org.bson.Document = {
      val params = updateDocument(flat)
      params.put("firstSeenAtEpoch",(new Date().getTime/1000).toString)
      params
    }
    if (flatsColl.findOneAndUpdate(findFilter(flat)
      ,new Document("$set",updateDocument(flat))) == null){
      flatsColl.insertOne(createDocument(flat))
      Logger.debug(s"Adding new flat $flat")
      Flat.Added
    }
    else {
      Logger.debug(s"Updating existing flat $flat")
      Flat.Updated
    }
  }

  def findHistoricAdds(flat: Flat): List[Flat] = {
    Logger.debug(s"Looking for flats matching: $flat")
    val documents = flatsColl.find(findFilter(flat))
    val cursor = documents.iterator()
    val flats = ListBuffer[Flat]()
    while (cursor.hasNext){
      val doc = cursor.next()
      flats += new Flat(
        Option(doc.get("address").toString),
        Option(doc.get("rooms").toString),
        Option(doc.get("size").toString.toInt),
        Option(doc.get("floor").toString),
        Option(doc.get("price").toString.toInt),
        Option(doc.get("link").toString),
        Option(doc.get("firstSeenAtEpoch").toString.toLong),
        Option(doc.get("lastSeenAtEpoch").toString.toLong)
      )
    }
    cursor.close()
    Logger.debug(s"Found $flats.size flats")
    flats.toList
  }

  private def findFilter(flat: Flat): org.bson.Document = {
    val params = new java.util.HashMap[String,Object]()
    params.put("floor",flat.floor.get)
    params.put("size",flat.size.get.toString)
    params.put("rooms",flat.rooms.get)
    params.put("address",flat.address.get)
    if (flat.price != None) params.put("price",flat.price.get.toString)
    if (flat.link != None) params.put("link",flat.link.get)
    new org.bson.Document(params)
  }


}

