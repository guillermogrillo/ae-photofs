package modules

import com.google.inject.AbstractModule

class ApplicationModule extends AbstractModule {
  override def configure() = {
    bind(classOf[ApplicationStart]).asEagerSingleton()
  }
}
