package scala.testhelpers

import configuration.MongoConnection
import configuration.testsupport.{MockedMongoConnection, MongoINMemoryDBStarter}
import org.scalatest.FlatSpec
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

object TestApplicationContextHelper extends FlatSpec {

  private val application = {
    MongoINMemoryDBStarter.startInMemoryMongo
    new GuiceApplicationBuilder()
      .overrides(bind[MongoConnection].to[MockedMongoConnection])
      .build()
  }

  def getGuiceContext(): Application = {
    application
  }

}
