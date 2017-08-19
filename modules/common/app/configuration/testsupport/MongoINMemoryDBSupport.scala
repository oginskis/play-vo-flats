package configuration.testsupport

import com.mongodb.{BasicDBObject, MongoClient}
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.{MongodConfigBuilder, Net}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network

object MongoINMemoryDBSupport {

  val InMemoryMongoHost = "localhost"
  val InMemoryMongoPort = 12345

  @volatile var isRunning:Boolean = false

  private val starter = MongodStarter.getDefaultInstance()
  private val mongodConfig = new MongodConfigBuilder()
    .version(Version.Main.PRODUCTION).net(new Net(InMemoryMongoHost, InMemoryMongoPort, Network.localhostIsIPv6)).build
  private val executable = starter.prepare(mongodConfig)

  def purgeFlats() = synchronized {
    if (isRunning == false) {
      throw new IllegalStateException("In-memory MongoDB is not running")
    }
    val mongo = new MongoClient(InMemoryMongoHost, InMemoryMongoPort)
    val db = mongo.getDatabase("test")
    val collection = db.getCollection("flats")
    val document = new BasicDBObject
    collection.deleteMany(document)
    mongo.close
  }

  def startInMemoryMongo = synchronized {
    if (isRunning == true) {
      throw new IllegalStateException("In-memory MongoDB is already running")
    }
    executable.start
    isRunning = true

  }

  def stopInMemoryMongo = synchronized {
    if (isRunning == false) {
      throw new IllegalStateException("In-memory MongoDB is not running")
    }
    executable.stop
    isRunning = false
  }

}
