package scala.testhelpers

import configuration.MongoConnection
import configuration.testsupport.{MockedMongoConnection, MongoINMemoryDBSupport}
import org.scalatest.FlatSpec
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

object TestApplicationContextHelper extends FlatSpec {

  private val application = {
    MongoINMemoryDBSupport.startInMemoryMongo
    new GuiceApplicationBuilder()
      .overrides(bind[MongoConnection].to[MockedMongoConnection])
      .configure("smtp.host" -> "localhost", "smtp.port" -> 2525)
      .build()
  }

  def getGuiceContext(): Application = {
    application
  }

}
