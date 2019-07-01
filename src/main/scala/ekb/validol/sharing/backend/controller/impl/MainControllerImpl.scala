package ekb.validol.sharing.backend.controller.impl

import ekb.validol.sharing.backend.controller.ApplicationController
import ekb.validol.sharing.backend.model.Protocol._
import ekb.validol.sharing.backend.model.Sharing
import ekb.validol.sharing.backend.storage.Storage
import ekb.validol.sharing.backend.storage.impl.InMemoryStorage.{StorageItem, StorageKey}
import ekb.validol.sharing.backend.utils.Validator

import scala.concurrent.{ExecutionContext, Future}

class MainControllerImpl(storage: Storage[StorageKey, StorageItem])(implicit ec: ExecutionContext) extends ApplicationController[ApiRequest, ApiResponse] {

  override def handleRequestAsync: PartialFunction[ApiRequest, Future[ApiResponse]] = {
    case ListRequest => handleListRequest()
    case req: AddRequest => validateAddRequest(req).flatMap(handleAddRequest)
    case PingRequest => Future.successful(PongResponse)
  }

  private def handleListRequest(): Future[ApiResponse] = {
    storage.list.map(ListResponse.apply)
  }

  private def handleAddRequest(req: AddRequest): Future[ApiResponse] = {
    storage.add(Sharing(req.users, req.selections)).map(SuccessResponse.apply)
  }

  private def validateAddRequest(req: AddRequest): Future[AddRequest] = {
    val errors = req.users.foldLeft(Vector.empty[String]) { (acc, item) =>
      Validator.isValidEmail(item) match {
        case Left(error) => acc :+ error
        case Right(_) => acc
      }
    }
    if (errors.nonEmpty) {
      Future.failed(ValidationError(errors))
    } else if (req.users.isEmpty || req.selections.isEmpty) {
      Future.failed(ValidationError(Seq("Invalid request")))
    } else Future.successful(req)
  }

}

object MainControllerImpl {

  def apply(storage: Storage[StorageKey, StorageItem])(implicit ec: ExecutionContext): MainControllerImpl = new MainControllerImpl(storage)
  
}
