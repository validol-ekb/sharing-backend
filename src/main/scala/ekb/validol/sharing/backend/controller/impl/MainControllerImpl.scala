package ekb.validol.sharing.backend.controller.impl

import ekb.validol.sharing.backend.controller.ApplicationController
import ekb.validol.sharing.backend.model.Protocol._
import ekb.validol.sharing.backend.model.Sharing
import ekb.validol.sharing.backend.storage.Storage
import ekb.validol.sharing.backend.storage.impl.InMemoryStorage.{StorageItem, StorageKey}

import scala.concurrent.{ExecutionContext, Future}

class MainControllerImpl(storage: Storage[StorageKey, StorageItem])(implicit ec: ExecutionContext) extends ApplicationController[ApiRequest, ApiResponse] {

  override def handleRequestAsync: PartialFunction[ApiRequest, Future[ApiResponse]] = {
    case ListRequest => handleListRequest()
    case req: AddRequest => handleAddRequest(req)
    case PingRequest => Future.successful(PongResponse)
  }

  private def handleListRequest(): Future[ApiResponse] = {
    storage.list.map(ListResponse.apply)
  }

  private def handleAddRequest(req: AddRequest): Future[ApiResponse] = {
    storage.add(Sharing(req.users, req.selections)).map(SuccessResponse.apply)
  }


}

object MainControllerImpl {

  def apply(storage: Storage[StorageKey, StorageItem])(implicit ec: ExecutionContext): MainControllerImpl = new MainControllerImpl(storage)
  
}