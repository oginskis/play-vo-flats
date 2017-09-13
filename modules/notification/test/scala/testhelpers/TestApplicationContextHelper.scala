package scala.testhelpers

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

object TestApplicationContextHelper {

  private val application = {
    new GuiceApplicationBuilder()
      .configure("smtp.host" -> "localhost",
        "smtp.port" -> "2525")
      .build()
  }

  def getGuiceContext(): Application = {
    application
  }
}
