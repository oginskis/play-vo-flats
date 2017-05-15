package repo

import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.{Inject, Singleton}

import com.mongodb.{MongoClient, MongoClientURI}
import model.Flat
import org.bson.Document
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
  * Created by oginskis on 30/12/2016.
  */
@Singleton
class FlatRepo @Inject() (configuration: play.api.Configuration) {

  val MONGODB_USER = "mongodb.user"
  val MONGODB_PASSWORD = "mongodb.password"
  val MONGODB_DB = "mongodb.db"
  val MONGODB_HOST = "mongodb.host"
  val MONGODB_PORT = "mongodb.port"
  val MONGODB_SSL = "mongodb.ssl"
  val COLL_NAME= "flats"

  val mongo = new MongoClient(new MongoClientURI("mongodb://"+configuration.underlying.getString(MONGODB_USER)+":"
    +configuration.underlying.getString(MONGODB_PASSWORD)+"@"+configuration.underlying.getString(MONGODB_HOST)
    +":"+configuration.underlying.getInt(MONGODB_PORT)+"/?ssl="+configuration.underlying.getBoolean(MONGODB_SSL)
    +"&maxIdleTimeMS=6000&minPoolSize=5"))
  val db = mongo.getDatabase(configuration.underlying.getString(MONGODB_DB))

  def addOrUpdateFlat(flat: Flat): Flat.Value = {
    def updateDocument(flat: Flat): org.bson.Document = {
      val params = new java.util.HashMap[String,Object]()
      params.put("address",flat.address.get)
      params.put("floor",flat.floor.get)
      params.put("link",flat.link.get)
      params.put("price",flat.price.get.toString)
      params.put("rooms",flat.rooms.get)
      params.put("size",flat.size.get.toString)
      params.put("lastSeenAt",new Date())
      new org.bson.Document(params)
    }
    def createDocument(flat: Flat): org.bson.Document = {
      val params = updateDocument(flat)
      params.put("firstSeenAt",new Date())
      params
    }
    if (db.getCollection(COLL_NAME).findOneAndUpdate(findFilter(flat)
      ,new Document("$set",updateDocument(flat))) == null){
      db.getCollection(COLL_NAME).insertOne(createDocument(flat))
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
    val documents = db.getCollection(COLL_NAME).find(findFilter(flat))
    val cursor = documents.iterator()
    val flats = ListBuffer[Flat]()
    while (cursor.hasNext){
      val doc = cursor.next()
      var firstSeenAt : Option[Date] = None
      if (doc.get("firstSeenAt") == null){
        firstSeenAt = Option(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("01-01-2017 00:00:00"))
      }
      else {
        firstSeenAt = Option(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
          .parse(doc.get("firstSeenAt").toString))
      }
      flats += new Flat(
        Option(doc.get("address").toString),
        Option(doc.get("rooms").toString),
        Option(doc.get("size").toString.toInt),
        Option(doc.get("floor").toString),
        Option(doc.get("price").toString.toInt),
        Option(doc.get("link").toString),
        firstSeenAt,
        Option(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(doc.get("lastSeenAt").toString))
      )
    }
    cursor.close()
    Logger.debug(s"Found $flats.size flats")
    flats.toList
  }

  def findFilter(flat: Flat): org.bson.Document = {
    val params = new java.util.HashMap[String,Object]()
    params.put("address",flat.address.get)
    params.put("floor",flat.floor.get)
    params.put("rooms",flat.rooms.get)
    params.put("size",flat.size.get.toString)
    if (flat.price != None) params.put("price",flat.price.get.toString)
    if (flat.link != None) params.put("link",flat.link.get)
    new org.bson.Document(params)
  }


}

