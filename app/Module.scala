import actors.ProcessingActor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    bindActor[ProcessingActor]("processingActor")
    bind(classOf[Scheduler]).asEagerSingleton()
  }

}
