package ekb.validol.sharing.backend.transport.impl.http

import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.ByteString
import ekb.validol.sharing.backend.model.Protocol
import ekb.validol.sharing.backend.model.Protocol._
import ekb.validol.sharing.backend.transport.ServerApp
import spray.json._
import ekb.validol.sharing.backend.model.Format._

import scala.concurrent.{ExecutionContext, Future}

trait HttpRequestHandler {
  this: ServerApp[HttpRequest, HttpResponse] =>

  implicit protected def materializer: Materializer

  implicit protected def ec: ExecutionContext

  override protected def handleAsync(in: HttpRequest): Future[HttpResponse] = {
    val p = convertRequest andThen { req =>
      req.flatMap { v =>
        controllers.find(_.handleRequestAsync.isDefinedAt(v)) match {
          case Some(c) => c.handleRequestAsync(v)
          case None => Future.successful(ErrorResponse("Can't find route for response"))
        }
      }
    } andThen { f =>
      f.map(convertResponse)
    }

    if (p.isDefinedAt(in)) {
      p(in).recover {
        case _: DeserializationException =>
          HttpResponse(StatusCodes.BadRequest)
        case err: ValidationError =>
          HttpResponse(StatusCodes.BadRequest, entity = HttpEntity(ContentTypes.`application/json`, err.toJson.compactPrint))
        case _: Throwable =>
          HttpResponse(StatusCodes.InternalServerError)
      }
    } else Future.successful(HttpResponse(StatusCodes.NotFound))
  }


  override def convertRequest: PartialFunction[HttpRequest, Future[Protocol.ApiRequest]] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/sharings"), _, _, _) => Future.successful(ListRequest)
    case HttpRequest(HttpMethods.POST, Uri.Path("/sharings"), _, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { bytes =>
        bytes.utf8String.parseJson.convertTo[AddRequest]
      }
  }


  override protected def convertResponse: PartialFunction[Protocol.ApiResponse, HttpResponse] = {
    case resp: ListResponse =>
      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, resp.toJson.compactPrint))
    case resp: SuccessResponse =>
      HttpResponse(StatusCodes.Accepted, entity = HttpEntity(ContentTypes.`application/json`, resp.toJson.compactPrint))
    case err: ErrorResponse =>
      HttpResponse(StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`application/json`, err.toJson.compactPrint))
  }


}
