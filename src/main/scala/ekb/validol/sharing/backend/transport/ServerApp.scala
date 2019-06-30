package ekb.validol.sharing.backend.transport

import ekb.validol.sharing.backend.controller.ApplicationController
import ekb.validol.sharing.backend.model.Protocol.{ApiRequest, ApiResponse}

import scala.concurrent.Future

trait ServerApp[In, Out] {

  def start(): Future[ServerInstance]

  def addHandler(controller: ApplicationController[ApiRequest, ApiResponse]): Unit

  protected def convertRequest: PartialFunction[In, Future[ApiRequest]]

  protected def convertResponse: PartialFunction[ApiResponse, Out]

  protected def handleAsync(in: In): Future[Out]

}
