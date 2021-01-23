package repositories

import models.ImageInfo

import javax.inject.Singleton
import scala.collection.mutable.ListBuffer
import scala.util.Try

@Singleton
class ImageRepository {

  private var images: ListBuffer[ImageInfo] = ListBuffer.empty[ImageInfo]

  def search(term: String) = {
    Try {
      images.filter(i =>
        i.id.toLowerCase.contains(term) ||
          i.author.toLowerCase.contains(term) ||
          i.camera.toLowerCase.contains(term) ||
          i.tags.toLowerCase.contains(term)
      ).toList
    }.toEither

  }

  def add(image: ImageInfo): Either[Throwable, ImageInfo] = synchronized {
    Try {
      images += image
      image
    }.toEither

  }

}
