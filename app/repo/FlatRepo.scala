package repo

import java.util
import java.util.Date
import javax.inject.{Inject, Singleton}

import com.mongodb.client.model.UpdateManyModel
import com.mongodb.{Block, MongoClient, MongoClientURI, QueryBuilder}
import model.b2c.{Flat, FlatPriceHistoryItem, SellerContactDetails}
import org.bson.Document
import org.bson.types.ObjectId
import play.api.Logger

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.Try

/**
  * Created by oginskis on 30/12/2016.
  */
@Singleton
class FlatRepo @Inject()(configuration: play.api.Configuration) {

  val flatsColl = new MongoClient(new MongoClientURI("mongodb://" +
    configuration.get[String](FlatRepo.MongoDbUser) + ":" +
    configuration.get[String](FlatRepo.MongoDbPassword) + "@" +
    configuration.get[String](FlatRepo.MongoDbHost) + ":" +
    configuration.get[Int](FlatRepo.MongoDbPort) + "/?ssl=" +
    configuration.get[Boolean](FlatRepo.MongoDbSsl) +
    configuration.get[String](FlatRepo.MongoDbAdditionalProps)))
    .getDatabase(configuration.get[String](FlatRepo.MongoDbDb))
    .getCollection(FlatRepo.CollName)

  def processFlats(batchSize: Int) = {
    val count = flatsColl.count(Document
      .parse(QueryBuilder.start().put("itemType").is("flat").get.toString))
    Logger.info(s"Number of flats: $count")
    val iterationsRequired = count / batchSize + 1
    Logger.info(s"Iterations required: $iterationsRequired")
    def processFlats(pageNumber: Int) {
      val docs = flatsColl.find()
        .skip(batchSize * (pageNumber-1))
        .limit(batchSize)
      Logger.info(s"Processing page $pageNumber out of $iterationsRequired")
      docs.forEach(new Block[Document]() {
        @Override
        def apply(document: Document) {
          val params = new util.HashMap[String,Object]()
          document.put("flatSearchString",document.get("address").toString+" "+document.get("flatFloor")+" "+
            document.get("numberOfRooms")+" "+document.get("price")+" "+document.get("size"))
          params.put("_id",new ObjectId(document.get("_id").toString))
          flatsColl.replaceOne(new org.bson.Document(params),document)
        }
      })
      if (pageNumber < iterationsRequired){
        processFlats(pageNumber+1)
      }
    }
    Logger.info("Started ad "+new Date().toString)
    processFlats(1)
    Logger.info("Stopped ad "+new Date().toString)
  }

  def expireFlats(olderThanMillis:Int) = {
    def updateDocument(): Document = {
      val params = new util.HashMap[String, Object]()
      params.put("expired","true")
      new Document(params)
    }
    val query = QueryBuilder.start().and(
      QueryBuilder.start().put("expired").is("false").get(),
      QueryBuilder.start().put("lastSeenAtEpoch").lessThan(java.lang.Long
        .valueOf((new Date().getTime/1000) - olderThanMillis)).get()
    ).get()
    val updates = util.Arrays.asList(
      new UpdateManyModel[Document](
        Document.parse(query.toString),
        new Document("$set",updateDocument())
        )
      );
    val bulkWriteResult = flatsColl.bulkWrite(updates)
    val modified = bulkWriteResult.getModifiedCount
    Logger.info(s"Expired $modified flats, which are not on ss.lv anymore")
  }

  private def getContactDetails(document: Document): Option[SellerContactDetails] = {
    if (document == null) return None
    Option(new SellerContactDetails(
      Option(Try(document.get("phoneNumbers").asInstanceOf[util.ArrayList[String]].asScala.toList)
        .getOrElse(List[String]()))
      ,
      if (document.getString("webPage") != null) {
        Option(document.getString("webPage"))
      } else {
        None
      },
      if (document.getString("company") != null) {
        Option(document.getString("company"))
      } else {
        None
      }
    ))
  }

  def getFlatById(flatId: String): Option[Flat] = {
    val params = new util.HashMap[String, Object]()
    params.put("_id", new ObjectId(flatId))
    val documents = flatsColl.find(new Document(params))
    if (documents.iterator().hasNext()) {
      val doc = documents.iterator().next()
      Some(new Flat(
        Flat.NA,
        Option(doc.get("address").toString),
        Option(doc.get("numberOfRooms").toString.toInt),
        Option(doc.get("size").toString.toInt),
        Option(doc.get("flatFloor").toString.toInt),
        Option(doc.get("maxFloors").toString.toInt),
        Option(doc.get("price").toString.toInt),
        Option(doc.get("link").toString),
        Option(doc.get("firstSeenAtEpoch").toString.toLong),
        Option(doc.get("lastSeenAtEpoch").toString.toLong),
        Option(doc.get("city").toString),
        Option(doc.get("district").toString),
        Option(doc.get("action").toString),
        Option(doc.get("expired").toString),
        Option(findFlatPriceHistoryItemsFor(new Flat(
          Option(doc.get("address").toString),
          Option(doc.get("numberOfRooms").toString.toInt),
          Option(doc.get("size").toString.toInt),
          Option(doc.get("flatFloor").toString.toInt),
          Option(doc.get("maxFloors").toString.toInt),
          Option(doc.get("price").toString.toInt),
          Option(doc.get("link").toString),
          Option(doc.get("city").toString),
          Option(doc.get("district").toString),
          Option(doc.get("action").toString)
        ))),
        getContactDetails(doc.get("sellerContactDetails").asInstanceOf[Document])
      ))
    } else {
      None
    }
  }

  def addOrUpdateFlat(flat: Flat): Flat = {
    val currentTimestamp = java.lang.Long.valueOf((new Date().getTime / 1000))
    def createSellerContactDetailsDocument(contactInformation:SellerContactDetails): org.bson.Document = {
      val params = new java.util.HashMap[String, Object]()
      if (contactInformation.company != None) params.put("company",contactInformation.company.get)
      if (contactInformation.webPage != None) params.put("webPage",contactInformation.webPage.get)
      if (contactInformation.phoneNumbers != None && contactInformation.phoneNumbers.get.size > 0){
        val phoneNumbers = new java.util.ArrayList[String]()
        contactInformation.phoneNumbers.get.foreach(phoneNumber => {
          phoneNumbers.add(phoneNumber)
        }
        )
        params.put("phoneNumbers",phoneNumbers)
      }
      new Document(params)
    }
    def updateDocument(flat: Flat): org.bson.Document = {
      val params = new java.util.HashMap[String, Object]()
      params.put("expired", "false")
      params.put("lastSeenAtEpoch", currentTimestamp)
      params.put("sellerContactDetails", createSellerContactDetailsDocument(flat.contactDetails.get))
      params.put("sellerSearchString",flat.contactDetails.get.company.getOrElse(""))
      new Document(params)
    }
    def createDocument(flat: Flat): org.bson.Document = {
      val params = new java.util.HashMap[String, Object]()
      params.put("address", flat.address.get)
      params.put("flatFloor", java.lang.Integer.valueOf(flat.floor.get))
      params.put("maxFloors", java.lang.Integer.valueOf(flat.maxFloors.get))
      params.put("link", flat.link.get)
      params.put("price", java.lang.Integer.valueOf(flat.price.get))
      params.put("numberOfRooms", java.lang.Integer.valueOf(flat.rooms.get))
      params.put("size", java.lang.Integer.valueOf(flat.size.get))
      params.put("city", flat.city.get)
      params.put("district", flat.district.get)
      params.put("action", flat.action.get)
      params.put("expired", "false")
      params.put("firstSeenAtEpoch",currentTimestamp)
      params.put("itemType", "flat")
      params.put("expired", "false")
      params.put("lastSeenAtEpoch",currentTimestamp)
      params.put("flatSearchString",flat.address.get +" "+ flat.floor.get +" "+ flat.rooms.get +" "+ flat.price.get +
        " "+flat.size.get)
      params.put("sellerSearchString",flat.contactDetails.get.company.getOrElse(""))
      params.put("sellerContactDetails",createSellerContactDetailsDocument(flat.contactDetails.get))
      new Document(params)
    }

    val updatedDocument = flatsColl.findOneAndUpdate(exactFindFilter(flat), new Document("$set", updateDocument(flat)))
    if (updatedDocument==null){
      flatsColl.insertOne(createDocument(flat))
      new Flat(
        Flat.New,
        flat.address,
        flat.rooms,
        flat.size,
        flat.floor,
        flat.maxFloors,
        flat.price,
        flat.link,
        Option(currentTimestamp),
        Option(currentTimestamp),
        flat.city,
        flat.district,
        flat.action,
        Option("false"),
        Option(findFlatPriceHistoryItemsFor(new Flat(
          flat.address,
          flat.rooms,
          flat.size,
          flat.floor,
          flat.maxFloors,
          flat.price,
          flat.link,
          flat.city,
          flat.district,
          flat.action
        ))),
        flat.contactDetails
      )
    } else {
      new Flat(
        Flat.SeenBefore,
        Option(updatedDocument.get("address").toString),
        Option(updatedDocument.get("numberOfRooms").toString.toInt),
        Option(updatedDocument.get("size").toString.toInt),
        Option(updatedDocument.get("flatFloor").toString.toInt),
        Option(updatedDocument.get("maxFloors").toString.toInt),
        Option(updatedDocument.get("price").toString.toInt),
        Option(updatedDocument.get("link").toString),
        Option(updatedDocument.get("firstSeenAtEpoch").toString.toLong),
        Option(currentTimestamp),
        Option(updatedDocument.get("city").toString),
        Option(updatedDocument.get("district").toString),
        Option(updatedDocument.get("action").toString),
        Option("false"),
        None,
        getContactDetails(updatedDocument.get("sellerContactDetails").asInstanceOf[Document])
      )
    }
  }

  def findFlatPriceHistoryItemsFor(flat: Flat): List[FlatPriceHistoryItem] = {
    val documents = flatsColl.find(exactFindFilter(
      new Flat(flat.address,
        flat.rooms,
        flat.size,
        flat.floor,
        flat.maxFloors,
        flat.city,
        flat.district,
        flat.action)
    ))
    val flatPriceHistoryItems = ListBuffer[FlatPriceHistoryItem]()
    val cursor = documents.iterator
    while (cursor.hasNext) {
      val document = cursor.next
      if (flat.link.get != document.get("link").toString ||
        flat.price.get != document.get("price").toString.toInt)
        flatPriceHistoryItems += FlatPriceHistoryItem(
          Option(document.get("link").toString),
          Option(document.get("price").toString.toInt),
          Option(document.get("firstSeenAtEpoch").toString.toLong),
          Option(document.get("lastSeenAtEpoch").toString.toLong),
          getContactDetails(document.get("sellerContactDetails").asInstanceOf[Document])
        )
    }
    flatPriceHistoryItems.toList
      .sortBy(_.lastSeenAt)(Ordering[Option[Long]])
      .sortBy(_.link)
      .reverse
  }

  private def exactFindFilter(flat: Flat): org.bson.Document = {
    val params = new java.util.HashMap[String, Object]()
    if (flat.floor != None) params.put("flatFloor", java.lang.Integer.valueOf(flat.floor.get))
    if (flat.maxFloors != None) params.put("maxFloors", java.lang.Integer.valueOf(flat.maxFloors.get))
    if (flat.size != None) params.put("size", java.lang.Integer.valueOf(flat.size.get))
    if (flat.rooms != None) params.put("numberOfRooms", java.lang.Integer.valueOf(flat.rooms.get))
    if (flat.address != None) params.put("address", flat.address.get)
    if (flat.city != None) params.put("city", flat.city.get)
    if (flat.district != None) params.put("district", flat.district.get)
    if (flat.action != None) params.put("action", flat.action.get)
    if (flat.price != None) params.put("price", java.lang.Integer.valueOf(flat.price.get))
    if (flat.link != None) params.put("link", flat.link.get)
    if (flat.expired != None) params.put("expired", flat.expired.get)
    new org.bson.Document(params)
  }
}

object FlatRepo {
  val MongoDbUser = "mongodb.user"
  val MongoDbDb = "mongodb.db"
  val MongoDbPort = "mongodb.port"
  val MongoDbPassword = "mongodb.password"
  val MongoDbHost = "mongodb.host"
  val MongoDbSsl = "mongodb.ssl"
  val CollName = "flats"
  val MongoDbAdditionalProps = "mongodb.additionalProps"
}

