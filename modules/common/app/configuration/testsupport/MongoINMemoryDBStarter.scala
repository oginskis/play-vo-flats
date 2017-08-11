package configuration.testsupport

import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.{MongodConfigBuilder, Net}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network

object MongoINMemoryDBStarter {

  val InMemoryMongoHost = "localhost"
  val InMemoryMongoPort = 12345

  private val starter = MongodStarter.getDefaultInstance()
  private val mongodConfig = new MongodConfigBuilder()
    .version(Version.Main.PRODUCTION).net(new Net(InMemoryMongoHost, InMemoryMongoPort, Network.localhostIsIPv6)).build
  private val executable = starter.prepare(mongodConfig)

  def startInMemoryMongo() = {
    executable.start
  }

  def stopInMemoryMongo() = {
    executable.stop
  }

}
