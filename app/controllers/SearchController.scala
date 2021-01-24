package controllers

import play.api.Logging
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{ImageService, SearchService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json


@Singleton
class SearchController @Inject()(searchService: SearchService,
                                 imageService: ImageService)(cc: ControllerComponents)(implicit ex: ExecutionContext) extends AbstractController(cc) with Logging {

  def search(term: String) = Action.async { implicit request =>
    logger.info(s"Received a request to search images by term $term")
    searchService.searchImages(term) map {
      case Right(images) =>
        logger.info(s"Found ${images.length} images with term $term")
        Ok(Json.toJson(images))
      case Left(ex: Throwable) =>
        logger.error(s"Received an error while searching images with term $term")
        throw ex
    }

  }

//  def refresh() = Action { implicit request =>
//    imageService.refreshAllImages()
//    Ok
//  }

}
