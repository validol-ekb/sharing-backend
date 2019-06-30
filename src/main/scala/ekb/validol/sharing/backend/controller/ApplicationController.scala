package ekb.validol.sharing.backend.controller

import ekb.validol.sharing.backend.model.Protocol.{ApiRequest, ApiResponse}

import scala.concurrent.Future

trait ApplicationController[In <: ApiRequest, Out <: ApiResponse] {

  def handleRequestAsync: PartialFunction[In, Future[Out]]

}
