package modules

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.{ImagesRefresherActor}

class ImageRefresherActorModule extends AbstractModule with AkkaGuiceSupport {
  override def configure = {
    bindActor[ImagesRefresherActor]("images-refresher")
    bindActor[ImagesRefresherActor]("image-detail-fetcher")
  }
}