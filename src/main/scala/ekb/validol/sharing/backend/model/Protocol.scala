package ekb.validol.sharing.backend.model

import java.util.UUID

object Protocol {

  sealed trait ApiRequest
  sealed trait ApiResponse

  case object PingRequest extends ApiRequest
  case object ListRequest extends ApiRequest
  case class AddRequest(users: Seq[String], selections: Map[SheetName.Value, Seq[Selection]]) extends ApiRequest

  case object PongResponse extends ApiResponse
  case class ListResponse(sharings: Map[UUID, Sharing]) extends ApiResponse
  case class SuccessResponse(id: UUID) extends ApiResponse
  case class ErrorResponse(msg: String) extends ApiResponse

}
