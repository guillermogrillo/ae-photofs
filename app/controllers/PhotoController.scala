package controllers

import play.api.{Logging, MarkerContext}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{AuthenticationService, PhotoService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import play.api.libs.json.{JsValue, Json}


@Singleton
class PhotoController @Inject()(photoService: PhotoService)(cc: ControllerComponents)(implicit ex: ExecutionContext) extends AbstractController(cc) with Logging {

  def getImagesPage = Action.async { implicit request =>
    logger.info(s"get all actions request received")
    photoService.getImagesPage(0) map {
      case Right(imagesPage) =>
        logger.info(s"Returning ${imagesPage.pictures.length} images")
        Ok(Json.toJson(imagesPage))
      case Left(ex: Throwable) =>
        logger.error("response error from list all actions service", ex)
        throw ex
    }
  }

  def search(term: String) = Action.async { implicit request =>
    logger.info(s"Received a request to search images by term $term")
    photoService.searchImages(term) map {
      case Right(images) =>
        logger.info(s"Found ${images.length} images with term $term")
        Ok(Json.toJson(images))
      case Left(ex: Throwable) =>
        logger.error(s"Received an error while searching images with term $term")
        throw ex
    }

  }



}
