package modules

import play.api.inject.SimpleModule
import play.api.inject._
import services.{ImagesRefreshTask}

class ImageRefresherTaskModule extends SimpleModule(bind[ImagesRefreshTask].toSelf.eagerly())
