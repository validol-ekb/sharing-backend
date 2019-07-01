package ekb.validol.sharing.backend.transport.impl.http

import java.util.concurrent.atomic.AtomicReference

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.typesafe.config.Config
import ekb.validol.sharing.backend.controller.ApplicationController
import ekb.validol.sharing.backend.model.Protocol._
import ekb.validol.sharing.backend.transport.{ServerApp, ServerInstance}

import scala.collection.immutable.HashSet
import scala.concurrent.{ExecutionContext, Future}
import spray.json._
import ekb.validol.sharing.backend.model.Format._

class HttpServerAppImpl(config: Config)(implicit system: ActorSystem, ec: ExecutionContext) extends ServerApp[HttpRequest, HttpResponse] {
  type Controller = ApplicationController[ApiRequest, ApiResponse]
  implicit private val materializer = ActorMaterializer()

  private val controllers = new AtomicReference[HashSet[Controller]](HashSet.empty[Controller])

  override def start(): Future[ServerInstance] = {
    Http()
      .bindAndHandleAsync(handleAsync, config.getString("host"), config.getInt("port"))
      .map(HttpServerInstance.apply)
  }

  override def addHandler(controller: Controller): Unit = {
    val cSet = controllers.get()
    val updated = cSet + controller
    if (!controllers.compareAndSet(cSet, updated)) {
      addHandler(controller)
    }
  }

  override protected def handleAsync(in: HttpRequest): Future[HttpResponse] = {
    val p = convertRequest andThen { req =>
        req.flatMap { v =>
          controllers.get().find(_.handleRequestAsync.isDefinedAt(v)) match {
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


  override protected def convertRequest: PartialFunction[HttpRequest, Future[ApiRequest]] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/sharings"), _, _, _) => Future.successful(ListRequest)
    case HttpRequest(HttpMethods.POST, Uri.Path("/sharings"), _, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { bytes =>
        bytes.utf8String.parseJson.convertTo[AddRequest]
      }
    case HttpRequest(HttpMethods.GET, Uri.Path("/ping"), _, _, _) => Future.successful(PingRequest)
  }

  override protected def convertResponse: PartialFunction[ApiResponse, HttpResponse] = {
    case PongResponse =>
      HttpResponse(StatusCodes.OK, entity = "PONG!")
    case resp: ListResponse =>
      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, resp.toJson.compactPrint))
    case SuccessResponse(id) =>
      HttpResponse(StatusCodes.Accepted, entity = HttpEntity(ContentTypes.`application/json`, JsObject("id" -> JsString(id.toString)).compactPrint))
    case ErrorResponse(msg) => HttpResponse(StatusCodes.NotFound, entity = msg)
  }
}

object HttpServerAppImpl {

  def apply(config: Config)(implicit system: ActorSystem, ec: ExecutionContext): HttpServerAppImpl = new HttpServerAppImpl(config)

}
