package services

import models.Credentials
import play.api.{Configuration, Logging}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.{HeaderNames, Headers, MimeTypes}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticationService @Inject()(ws: WSClient, configuration: Configuration)(implicit ec: ExecutionContext) extends Logging {

  val authenticationUrl = configuration.get[String]("ae.authentication.url")
  val authenticationApikey = configuration.get[String]("ae.authentication.apikey")

  var bearerToken = ""

  def authenticate(): Future[Either[Throwable, String]] = {

    val apiKeyData = Json.obj(
      "apiKey" -> authenticationApikey
    )
    logger.info("Creating Authentication Request")
    ws.url(authenticationUrl)
      .withHttpHeaders(HeaderNames.CONTENT_TYPE -> MimeTypes.JSON)
      .post(apiKeyData)
      .map{ httpResponse: WSResponse => {
        logger.info(s"Got back from authentication service with response ${httpResponse.status} and ${httpResponse.json}")
        httpResponse.status match {
        case 200 =>
          Credentials.format.reads(httpResponse.json)
            .fold(
              invalid => {
                Left(new RuntimeException("Could not parse the response " + invalid.toString()))
              },
              aeCredential => {
                bearerToken = aeCredential.token
                Right(aeCredential.token)
              }
            )
        case error =>
          Left(new RuntimeException("Could not authenticate"))
      }}}
  }

  def getBearerToken(): Future[Either[Throwable, String]] = Future.successful(Right(bearerToken))

}
