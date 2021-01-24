package services

import akka.actor.{Actor, ActorRef, ActorSystem}
import play.api.Configuration

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


class ImagesRefreshTask @Inject()(actorSystem: ActorSystem, @Named("images-refresher") imageRefresherActor: ActorRef, configuration: Configuration)(
  implicit executionContext: ExecutionContext
) {

  val refreshInterval = configuration.get[Long]("ae.refresh-frequency")

  actorSystem.scheduler.scheduleAtFixedRate(
    initialDelay = 0.millisecond,
    interval = refreshInterval.millisecond,
    receiver = imageRefresherActor,
    message = "refresh"
  )

}

class ImagesRefresherActor @Inject()(photoService: ImageService) extends Actor {
  def receive = {
    case "refresh" =>
      photoService.refreshAllImages()
  }
}
