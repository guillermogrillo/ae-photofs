package services

import akka.actor.ActorSystem
import play.api.Logging
import services.ImageDetailFetchActor.FetchDetails

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImageService @Inject()(externalPhotoService: ExternalPhotoService, actorSystem: ActorSystem)(implicit ec: ExecutionContext) extends Logging {

  def refreshAllImages() = {
    println("I am refreshing images")
    var hasMore = true
    var iteration = 0
    while (hasMore) {
      for {
        pageResponse <- externalPhotoService.getImagesPage(iteration)
        _ <-
          pageResponse match {
            case Left(ex) =>
              logger.error(s"Error while searching page info with iteration $iteration", ex)
              Future.successful(Right())
            case Right(page) =>
              hasMore = page.hasMore
              iteration += 1
              val imageDetailFetcherActor = actorSystem.actorOf(ImageDetailFetchActor.props, "image-detail-fetch-actor" )
              imageDetailFetcherActor ! FetchDetails(page)
              Future.successful(Right())
          }
      } yield()
    }
  }

}
