package services

import play.api.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetryableService @Inject()(authenticationService: AuthenticationService) extends Logging {

  //this could be parametrized in configs
  val retries: Int = 1

  def executeWithRetries[T]( f: => Future[Either[Throwable, T]], iteration: Int = 1, ex: ExecutionContext):  Future[Either[Throwable, T]]= {
    implicit val executionContext = ex
    f.map {
      case Left(ex) => {
        if (iteration <= retries) {
          logger.error(s"Retry $iteration out of $retries")
          authenticationService.authenticate() map {
            case Left(error) =>
              logger.error(s"There was an exception trying to authenticate the request. Attempt $iteration out of $retries", error)
              executeWithRetries(f, iteration + 1, executionContext)
            case Right(_) =>
              executeWithRetries(f, iteration + 1, executionContext)
          }
        } else {
          logger.error("There was an error and there are no more retries")
          Future.successful(Left(ex))
        }
      }
    }
  }

}
