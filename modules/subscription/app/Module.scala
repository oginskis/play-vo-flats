import com.google.inject.AbstractModule
import configuration.{DefaultMongoConnection, MongoConnection}
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bind(classOf[MongoConnection])
      .to(classOf[DefaultMongoConnection])
  }
}
