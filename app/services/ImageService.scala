package services

import akka.actor.ActorSystem
import play.api.Logging
import services.ImageDetailFetchActor.FetchDetails

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import cats.data.EitherT
import cats.implicits._
import models.ImagesPage
import repositories.ImageRepository

import scala.util.Try

class ImageService @Inject()(externalPhotoService: ExternalPhotoService, authenticationService: AuthenticationService, imageRepository: ImageRepository, actorSystem: ActorSystem)(implicit ec: ExecutionContext) extends Logging {


  def refreshAllImages() = {
    logger.info("I am refreshing images")

    for {
      response <- EitherT(externalPhotoService.getImagesPage(0))
      _ <- EitherT(handlePages(response.pageCount))
    } yield()
    logger.info("Refreshing task finished")
  }

  def handlePages(totalPages: Int): Future[Either[Throwable, Unit]] = {
    for (i <- 0 to totalPages) {
      for {
        pageResponse <- EitherT(externalPhotoService.getImagesPage(i))
        _ <- EitherT(handlePage(pageResponse))
      } yield ()
    }
    Future.successful(Right(()))
  }

  def handlePage(imagesPage: ImagesPage): Future[Either[Throwable, Unit]] = Future {
    //    Future.successful(Right(actorSystem.actorOf(ImageDetailFetchActor.props, "image-detail-fetcher" ) ! FetchDetails(imagesPage)))
    Try {
      imagesPage.pictures.foreach(p => {
        for {
          image <- EitherT(externalPhotoService.getImageInfo(p.id))
          _ <- EitherT(Future.successful(imageRepository.add(image)))
        } yield ()
      })
    }.toEither
  }

}
