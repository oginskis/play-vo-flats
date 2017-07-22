import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    bind(classOf[Scheduler]).asEagerSingleton()
  }

}
