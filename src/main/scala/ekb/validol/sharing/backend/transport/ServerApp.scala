package ekb.validol.sharing.backend.transport

import java.util.concurrent.atomic.AtomicReference

import ekb.validol.sharing.backend.controller.ApplicationController
import ekb.validol.sharing.backend.model.Protocol.{ApiRequest, ApiResponse}

import scala.collection.immutable.HashSet
import scala.concurrent.Future

trait ServerApp[In, Out] {
  type Controller = ApplicationController[ApiRequest, ApiResponse]

  private val controllersMap = new AtomicReference[HashSet[Controller]](HashSet.empty[Controller])

  def addHandler(controller: Controller): Unit = {
    val cSet = controllersMap.get()
    val updated = cSet + controller
    if (!controllersMap.compareAndSet(cSet, updated)) {
      addHandler(controller)
    }
  }

  def start(): Future[ServerInstance]

  protected def controllers: HashSet[Controller] = controllersMap.get()

  protected def convertRequest: PartialFunction[In, Future[ApiRequest]]

  protected def convertResponse: PartialFunction[ApiResponse, Out]

  protected def handleAsync(in: In): Future[Out]

}
