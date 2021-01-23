package services

import akka.actor.{Actor, Props}
import models.ImagesPage
import play.api.Logging
import repositories.ImageRepository
import services.ImageDetailFetchActor.FetchDetails

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImageDetailFetchActor @Inject()(externalPhotoService: ExternalPhotoService, imageRepository: ImageRepository)(implicit ex: ExecutionContext) extends Actor with Logging {
  override def receive: Receive = {
    case FetchDetails(imagesPage) =>
      logger.info(s"I am the actor in charge of searching the details in page ${imagesPage.page}")
      imagesPage.pictures.foreach(p => {
        for {
          imageInfoResponse <- Future.successful(externalPhotoService.getImageInfo(p.id))
          result <- imageInfoResponse map { response =>
            response match {
              case Left(ex) =>
                logger.error("Error fetching image info" , ex)
              case Right(image) =>
                Future.successful(imageRepository.add(image))
            }
          }
        } yield()
      })
  }
}

object ImageDetailFetchActor {
  def props  = Props[ImageDetailFetchActor]
  case class FetchDetails(imagesPage: ImagesPage)
}
