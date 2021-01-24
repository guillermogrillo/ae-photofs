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
        i.id.toLowerCase.contains(term.toLowerCase) ||
          i.author.toLowerCase.contains(term.toLowerCase) ||
          i.camera.toLowerCase.contains(term.toLowerCase) ||
          i.tags.toLowerCase.contains(term.toLowerCase)
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
