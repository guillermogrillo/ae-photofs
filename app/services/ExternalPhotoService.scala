package services

import models.{ImageInfo, ImagesPage}
import play.api.{Configuration, Logging}
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import cats.data.EitherT
import cats.implicits._

class ExternalPhotoService @Inject()(ws: WSClient,
                                     configuration: Configuration,
                                     retryableService: RetryableService,
                                     authenticationService: AuthenticationService)(implicit ex: ExecutionContext) extends Logging {


  val imagesUrl = configuration.get[String]("ae.images.url")

//  def getImagesPage(page: Int): Future[Either[Throwable, ImagesPage]] = {
//
//    retryableService.executeWithRetries((for{
//      token <- authenticationService.getBearerToken()
//      response <-
//          ws.url(imagesUrl)
//            .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${token}")
//            .addQueryStringParameters("page" -> page.toString)
//            .get()
//      pageResponse <- Future.successful(response.status match {
//        case 200 =>
//          ImagesPage.format.reads(response.json)
//            .fold(
//              invalid => Left(new RuntimeException("Could not parse the response " + invalid.toString())),
//              images => Right(ImagesPage(images.pictures, images.page, images.pageCount, images.hasMore))
//            )
//        case _ =>
//          Left(new RuntimeException("Error while searching images page"))
//      })
//    } yield(pageResponse)), ex = ex)
//  }

  def getImagesPage(page: Int): Future[Either[Throwable, ImagesPage]] = {
    logger.info("Starting ImagesPage request")
    (for {
      token <- EitherT(authenticationService.getBearerToken())
      response <- EitherT(getImagesPage(page, token))
    } yield (response)).value
  }

  private def getImagesPage(page: Int, token: String): Future[Either[Throwable, ImagesPage]] = {
    logger.info("Creating GetImagesPage Request")
    ws.url(imagesUrl)
      .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${token}")
      .addQueryStringParameters("page" -> page.toString)
      .get()
      .map { wsResponse =>
        logger.info(s"Got back from images service with response ${wsResponse.status} and ${wsResponse.json}")
        wsResponse.status match {
          case 200 =>
            ImagesPage.format.reads(wsResponse.json).fold(
              _ => Left(new RuntimeException(s"Could not parse the response ${wsResponse.json} as a ImagesPage")),
              images => Right(ImagesPage(images.pictures, images.page, images.pageCount, images.hasMore))
            )
          case _ => Left(new RuntimeException("Error while searching images page"))
        }
      }
  }

  def getImageInfo(id: String): Future[Either[Throwable, ImageInfo]] = {
      ((for {
        token <- EitherT(authenticationService.getBearerToken())
        response <- EitherT(getImageInfo(id, token))
      } yield (response)).value)
  }

  private def getImageInfo(id: String, token: String): Future[Either[Throwable, ImageInfo]] = {
    logger.info("Creating GetImageInfo Request")

    ws.url(s"$imagesUrl/$id")
      .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${token}")
      .get()
      .map { wsResponse =>
        logger.info(s"Got back from image detail service with response ${wsResponse.status} and ${wsResponse.json}")
        wsResponse.status match {
          case 200 =>
            ImageInfo.format.reads(wsResponse.json).fold(
              _ => Left(new RuntimeException(s"Could not parse the response ${wsResponse.json} as a ImagesPage")),
              image => Right(ImageInfo(image.id, image.author, image.camera, image.tags, image.cropped_picture, image.full_picture))
            )
          case _ => Left(new RuntimeException("Error while searching images page"))
        }
      }
  }

}
