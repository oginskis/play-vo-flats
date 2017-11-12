package repo

import java.util
import java.util.Date
import javax.inject.{Inject, Singleton}

import com.mongodb.QueryBuilder
import com.mongodb.client.model.UpdateManyModel
import configuration.MongoConnection
import model.CommonProps
import model.b2c.{Flat, FlatPriceHistoryItem, SellerContactDetails}
import org.bson.Document
import org.bson.types.ObjectId
import play.api.Logger
import play.shaded.ahc.io.netty.util.internal.StringUtil

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Created by oginskis on 30/12/2016.
  */
@Singleton
class FlatRepo @Inject()(connection: MongoConnection) {

  private val flatsCollection = connection.getCollection(FlatRepo.CollName)

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
    val bulkWriteResult = flatsCollection.bulkWrite(updates)
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
    if (StringUtil.isNullOrEmpty(flatId)){
      return None
    }
    val params = new util.HashMap[String, Object]()
    params.put("_id", new ObjectId(flatId))
    val documents = flatsCollection.find(new Document(params))
    val flatList = documents.asScala.toList.map(doc => {
      Flat(
        address = Option(doc.get("address").toString),
        rooms = Option(doc.get("numberOfRooms").toString.toInt),
        size = Option(doc.get("size").toString.toInt),
        floor = Option(doc.get("flatFloor").toString.toInt),
        maxFloors = Option(doc.get("maxFloors").toString.toInt),
        price = Option(doc.get("price").toString.toInt),
        link = Option(doc.get("link").toString),
        firstSeenAt = Option(doc.get("firstSeenAtEpoch").toString.toLong),
        lastSeenAt = Option(doc.get("lastSeenAtEpoch").toString.toLong),
        city = Option(doc.get("city").toString),
        district = Option(doc.get("district").toString),
        action = Option(doc.get("action").toString),
        expired = Option(doc.get("expired").toString),
        flatPriceHistoryItems = Option(findFlatPriceHistoryItemsFor(Flat(
          address = Option(doc.get("address").toString),
          rooms = Option(doc.get("numberOfRooms").toString.toInt),
          size = Option(doc.get("size").toString.toInt),
          floor = Option(doc.get("flatFloor").toString.toInt),
          maxFloors = Option(doc.get("maxFloors").toString.toInt),
          price = Option(doc.get("price").toString.toInt),
          link = Option(doc.get("link").toString),
          city = Option(doc.get("city").toString),
          district = Option(doc.get("district").toString),
          action = Option(doc.get("action").toString)
        ))),
        contactDetails = getContactDetails(doc.get("sellerContactDetails").asInstanceOf[Document]))
    })
    if (flatList.size > 0){
      return Option(flatList.head)
    }
    else {
      return None
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
      params.put("address", flat.address.getOrElse(CommonProps.EmptyProp))
      params.put("flatFloor", java.lang.Integer.valueOf(flat.floor.getOrElse(0)))
      params.put("maxFloors", java.lang.Integer.valueOf(flat.maxFloors.getOrElse(0)))
      params.put("link", flat.link.getOrElse(CommonProps.EmptyProp))
      params.put("price", java.lang.Integer.valueOf(flat.price.getOrElse(0)))
      params.put("numberOfRooms", java.lang.Integer.valueOf(flat.rooms.get))
      params.put("size", java.lang.Integer.valueOf(flat.size.getOrElse(0)))
      params.put("city", flat.city.getOrElse(CommonProps.EmptyProp))
      params.put("district", flat.district.getOrElse(CommonProps.EmptyProp))
      params.put("action", flat.action.getOrElse(CommonProps.EmptyProp))
      params.put("buildingType", flat.buildingType.getOrElse(CommonProps.EmptyProp))
      params.put("expired", "false")
      params.put("firstSeenAtEpoch",currentTimestamp)
      params.put("itemType", "flat")
      params.put("expired", "false")
      params.put("lastSeenAtEpoch",currentTimestamp)
      params.put("flatSearchString",flat.address.getOrElse(CommonProps.EmptyProp)
        +" "+ flat.floor.getOrElse(CommonProps.EmptyProp) +" "+ flat.rooms.getOrElse(CommonProps.EmptyProp)
        +" "+ flat.price.getOrElse(CommonProps.EmptyProp) +
        " "+flat.size.getOrElse(CommonProps.EmptyProp))
      params.put("sellerSearchString",flat.contactDetails.get.company.getOrElse(""))
      params.put("sellerContactDetails",createSellerContactDetailsDocument(flat.contactDetails.get))
      new Document(params)
    }

    val updatedDocument = flatsCollection.findOneAndUpdate(exactFindFilter(flat), new Document("$set", updateDocument(flat)))
    if (updatedDocument==null){
      flatsCollection.insertOne(createDocument(flat))
      Flat(
        status = Option("New"),
        address = flat.address,
        rooms = flat.rooms,
        size = flat.size,
        floor = flat.floor,
        maxFloors = flat.maxFloors,
        price = flat.price,
        buildingType = flat.buildingType,
        link = flat.link,
        firstSeenAt = Option(currentTimestamp),
        lastSeenAt = Option(currentTimestamp),
        city = flat.city,
        district = flat.district,
        action = flat.action,
        expired = Option("false"),
        flatPriceHistoryItems = Option(findFlatPriceHistoryItemsFor(Flat(
          address = flat.address,
          rooms = flat.rooms,
          size = flat.size,
          floor = flat.floor,
          maxFloors = flat.maxFloors,
          price = flat.price,
          link = flat.link,
          city = flat.city,
          district = flat.district,
          action = flat.action
        ))),
        contactDetails = flat.contactDetails
      )
    } else {
      Flat(
        status = Option("SeenBefore"),
        address = Option(updatedDocument.get("address").toString),
        rooms = Option(updatedDocument.get("numberOfRooms").toString.toInt),
        size = Option(updatedDocument.get("size").toString.toInt),
        floor = Option(updatedDocument.get("flatFloor").toString.toInt),
        maxFloors = Option(updatedDocument.get("maxFloors").toString.toInt),
        price = Option(updatedDocument.get("price").toString.toInt),
        buildingType = Option(
          if (updatedDocument.get("buildingType") == null) {
            CommonProps.EmptyProp
          } else updatedDocument.get("buildingType").toString
        ),
        link = Option(updatedDocument.get("link").toString),
        firstSeenAt = Option(updatedDocument.get("firstSeenAtEpoch").toString.toLong),
        lastSeenAt = Option(currentTimestamp),
        city = Option(updatedDocument.get("city").toString),
        district = Option(updatedDocument.get("district").toString),
        action = Option(updatedDocument.get("action").toString),
        expired = Option("false"),
        contactDetails = getContactDetails(updatedDocument.get("sellerContactDetails").asInstanceOf[Document])
      )
    }
  }

  def findFlatPriceHistoryItemsFor(flat: Flat): List[FlatPriceHistoryItem] = {
    val documents = flatsCollection.find(exactFindFilter(
      Flat(address = flat.address,
        rooms = flat.rooms,
        size = flat.size,
        floor = flat.floor,
        maxFloors = flat.maxFloors,
        city = flat.city,
        district = flat.district,
        action = flat.action)
    ))
    return documents.asScala.toList.map(document => {
        FlatPriceHistoryItem(
          Option(document.get("link").toString),
          Option(document.get("price").toString.toInt),
          Option(document.get("firstSeenAtEpoch").toString.toLong),
          Option(document.get("lastSeenAtEpoch").toString.toLong),
          getContactDetails(document.get("sellerContactDetails").asInstanceOf[Document])
        )
    }).filterNot(historyItem => {
      historyItem.link.get == flat.link.get &&
      historyItem.price.get == flat.price.get
    }).sortBy(
      _.lastSeenAt
    ).reverse
  }

  private def exactFindFilter(flat: Flat): org.bson.Document = {
    val params = new java.util.HashMap[String, Object]()
    for (floor <- flat.floor){
      params.put("flatFloor", java.lang.Integer.valueOf(floor))
    }
    for (maxFloors <- flat.maxFloors){
      params.put("maxFloors", java.lang.Integer.valueOf(maxFloors))
    }
    for (size <- flat.size) {
      params.put("size", java.lang.Integer.valueOf(size))
    }
    for (rooms <- flat.rooms) {
      params.put("numberOfRooms", java.lang.Integer.valueOf(rooms))
    }
    for (address <- flat.address) {
      params.put("address", address)
    }
    for (city <- flat.city) {
      params.put("city", city)
    }
    for (district <- flat.district) {
      params.put("district", district)
    }
    for (action <- flat.action) {
      params.put("action", action)
    }
    for (price <- flat.price) {
      params.put("price", java.lang.Integer.valueOf(price))
    }
    for (link <- flat.link) {
      params.put("link", link)
    }
    for (expired <- flat.expired) {
      params.put("expired", expired)
    }
    new org.bson.Document(params)
  }
}

object FlatRepo {
  val CollName = "flats"
}

