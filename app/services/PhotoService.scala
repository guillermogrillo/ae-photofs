package services

import models.{ImageInfo, ImagesPage}
import play.api.Configuration
import play.api.libs.ws.{WSClient}
import play.mvc.Http.HeaderNames

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PhotoService @Inject()(ws: WSClient, configuration: Configuration, authenticationService: AuthenticationService)(implicit ec: ExecutionContext)  {

  val imagesUrl = configuration.get[String]("ae.images.url")

  //use cache instead
  val guillermoPhoto = ImageInfo("1", "guillermo" , "nikon 1234" , "#photo #test", "http://cropped.com", "http://full.com")
  val mockedImagesByIdMap = Map("1" -> guillermoPhoto)
  val mockedImagesByAuthorMap = Map("guillermo" -> guillermoPhoto)
  val mockedImagesByCameraMap = Map("nikon 1234" -> guillermoPhoto)
  val mockedImagesByTagsMap = Map("#photo #test" -> guillermoPhoto)

  def refreshAllImages() = {
    println("I am refreshing images")
  }

  def searchImages(term: String): Future[Either[Throwable, List[ImageInfo]]] = {
    Future.successful(Right(List(mockedImagesByIdMap(term))))
  }

  def getImagesPage(page: Int) = {

    for {
      token <- authenticationService.authenticate()
      response <- token match {
        case Right(bearerToken) =>
          ws.url(imagesUrl)
            .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${bearerToken}")
            .addQueryStringParameters("page" -> page.toString)
            .get()
        case _ =>
          throw new RuntimeException("Could not authenticate")
      }
      imagesPage <- Future.successful(response.status match {
        case 200 =>
          ImagesPage.format.reads(response.json)
            .fold(
              invalid => {
                Left(new RuntimeException("Could not parse the response " + invalid.toString()))
              },
              images => {
                println(s"Found ${images.pageCount} pages")
                //save to cache
                Right(ImagesPage(images.pictures, images.page, images.pageCount, images.hasMore))
              }
            )
        case other =>
          Left(new RuntimeException("Could not authenticate"))
      })
    } yield imagesPage
  }

  def getImageInfo(id: String) = {
    for {
      bearerToken <- Future.successful(authenticationService.getBearerToken())
      response <- ws.url(s"imagesUrl/$id")
        .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${bearerToken}")
        .get()
      imageInfo <- Future.successful(response.status match {
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
        case other =>
          Left(new RuntimeException("Could not authenticate"))
      })
    } yield (imageInfo)



  }

}
