package models

import play.api.libs.json.{Format, Json}

case class Credentials(token: String, auth: Boolean)

object Credentials {
  implicit val format: Format[Credentials] = Json.format
//  implicit val writes: Writes[Credentials] = Json.writes[Credentials]
//  implicit val reads: Reads[Credentials] = Json.reads[Credentials]
}

case class Picture(id: String, cropped_picture: String)

object Picture {
  implicit val format: Format[Picture] = Json.format
}

case class ImagesPage(pictures: List[Picture], page: Int, pageCount: Int, hasMore: Boolean)

object ImagesPage {
  implicit val format: Format[ImagesPage] = Json.format
}

case class ImageInfo(id: String, author: String, camera: String, tags: String, cropped_picture: String, full_picture: String)

object ImageInfo {
  implicit val format: Format[ImageInfo] = Json.format
}

