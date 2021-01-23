package services

import models.{ImageInfo, ImagesPage}
import play.api.{Configuration, Logging}
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExternalPhotoService @Inject()(ws: WSClient,
                                     configuration: Configuration,
                                     retryableService: RetryableService,
                                     authenticationService: AuthenticationService)(implicit ex: ExecutionContext) extends Logging {


  val imagesUrl = configuration.get[String]("ae.images.url")

  def getImagesPage(page: Int): Future[Either[Throwable, ImagesPage]] = {

    retryableService.executeWithRetries((for{
      token <- authenticationService.getBearerToken()
      response <-
          ws.url(imagesUrl)
            .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${token}")
            .addQueryStringParameters("page" -> page.toString)
            .get()
      pageResponse <- Future.successful(response.status match {
        case 200 =>
          ImagesPage.format.reads(response.json)
            .fold(
              invalid => Left(new RuntimeException("Could not parse the response " + invalid.toString())),
              images => Right(ImagesPage(images.pictures, images.page, images.pageCount, images.hasMore))
            )
        case _ =>
          Left(new RuntimeException("Error while searching images page"))
      })
    } yield(pageResponse)), ex = ex)
  }

  def getImageInfo(id: String): Future[Either[Throwable, ImageInfo]] = {
    retryableService.executeWithRetries(
      (for {
        token <- authenticationService.getBearerToken()
        response <-
            ws.url(s"$imagesUrl/$id")
              .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${token}")
              .get()
        imageInfo <- Future.successful(
          response.status match {
            case 200 =>
              ImageInfo.format.reads(response.json)
                .fold(
                  invalid => {
                    Left(new RuntimeException("Could not parse the response " + invalid.toString()))
                  },
                  image => {
                    Right(ImageInfo(image.id, image.author, image.camera, image.tags, image.cropped_picture, image.full_picture))
                  }
                )
            case _ =>
              Left(new RuntimeException("Could not authenticate"))
          })
      } yield (imageInfo)),ex = ex)
  }

}
