package services

import models.Credentials
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.{HeaderNames, Headers, MimeTypes}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class AuthenticationService @Inject()(ws: WSClient, configuration: Configuration)(implicit ec: ExecutionContext) {

  val authenticationUrl = configuration.get[String]("ae.authentication.url")
  val authenticationApikey = configuration.get[String]("ae.authentication.apikey")

  var bearerToken = ""

  def authenticate(): Future[Either[Throwable, String]] = {

    val apiKeyData = Json.obj(
      "apiKey" -> authenticationApikey
    )

    ws.url(authenticationUrl)
      .withHttpHeaders(HeaderNames.CONTENT_TYPE -> MimeTypes.JSON)
      .post(apiKeyData)
      .map{ httpResponse: WSResponse => httpResponse.status match {
        case 200 =>
          Credentials.format.reads(httpResponse.json)
            .fold(
              invalid => {
                Left(new RuntimeException("Could not parse the response " + invalid.toString()))
              },
              aeCredential => {
                Right(aeCredential.token)
              }
            )
        case error =>
          Left(new RuntimeException("Could not authenticate"))
      }}
  }

  def getBearerToken(): String = {
    if (bearerToken.equals("")) {
      authenticate() map { response =>
        response match {
          case Right(value) => bearerToken = value
          case Left(error) => throw error
        }
      }

    }
    bearerToken
  }

}
