package configuration
import com.mongodb.MongoClient
import com.mongodb.client.{MongoCollection, MongoDatabase}
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.{MongodConfigBuilder, Net}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.bson.Document

class MockedMongoConnection extends MongoConnection{

  val mongoDb: MongoDatabase = {
    val starter = MongodStarter.getDefaultInstance();
    val bindIp = "localhost"
    val port = 12345
    val mongodConfig = new MongodConfigBuilder()
      .version(Version.Main.PRODUCTION).net(new Net(bindIp, port, Network.localhostIsIPv6)).build
    val executable = starter.prepare(mongodConfig)
    executable.start
    val mongo = new MongoClient(bindIp, port)
    val db = mongo.getDatabase("test")
    db.createCollection("flats")
    db
  }

  override def getCollection(collectionName: String): MongoCollection[Document] = {
    mongoDb.getCollection(collectionName)
  }
}
