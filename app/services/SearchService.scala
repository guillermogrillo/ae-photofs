package services

import models.ImageInfo
import repositories.ImageRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class SearchService @Inject()(imageRepository: ImageRepository)(implicit ex: ExecutionContext) {

  def searchImages(term: String): Future[Either[Throwable, List[ImageInfo]]] = {
    Future.successful(imageRepository.search(term))
  }


}
