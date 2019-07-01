package ekb.validol.sharing.backend.transport

import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, Materializer}
import ekb.validol.sharing.backend.model.Protocol._
import ekb.validol.sharing.backend.transport.impl.http.HttpRequestHandler
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.mockito.Mockito.{mock, times, verify, when}
import spray.json.{DeserializationException, JsArray}

import scala.collection.immutable.HashSet
import scala.concurrent.{ExecutionContext, Future}

class HttpRequestHandlerTest extends FreeSpec with ScalaFutures with HttpRequestHandler with ServerApp[HttpRequest, HttpResponse]{

  private val controller = mock(classOf[Controller])

  override implicit protected def materializer: Materializer = mock(classOf[ActorMaterializer])

  override implicit protected val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def start(): Future[ServerInstance] = Future.successful(mock(classOf[ServerInstance]))

  override def controllers: HashSet[Controller] = HashSet(controller)

  "HttpRequestHandler" - {

    "should successfully handle list request" in {
      val req = HttpRequest(HttpMethods.GET, Uri("/sharings"))
      val resp = ListResponse(Map.empty)
      val funMock = mock(classOf[PartialFunction[ApiRequest, Future[ApiResponse]]])

      when(controller.handleRequestAsync).thenReturn(funMock)
      when(funMock.isDefinedAt(ListRequest)).thenReturn(true)
      when(controller.handleRequestAsync(ListRequest)).thenReturn(Future.successful(resp))

      whenReady(handleAsync(req)) { resp =>
        assert(resp.status == StatusCodes.OK)
        assert(resp.entity == HttpEntity(ContentTypes.`application/json`, JsArray(Vector.empty).compactPrint))
      }
    }

    "should successfully handle unknown request with 404" in {
      val req = HttpRequest(HttpMethods.GET, Uri("/unknown"))

      whenReady(handleAsync(req)) { resp =>
        assert(resp.status == StatusCodes.NotFound)
      }
    }

    "should successfully handle request without handler" in {
      val req = HttpRequest(HttpMethods.GET, Uri("/sharings"))
      val funMock = mock(classOf[PartialFunction[ApiRequest, Future[ApiResponse]]])

      when(controller.handleRequestAsync).thenReturn(funMock)
      when(funMock.isDefinedAt(ListRequest)).thenReturn(false)

      whenReady(handleAsync(req)) { resp =>
        assert(resp.status == StatusCodes.NotFound)
      }
    }

    "should successfully handle request if controller returned validation error" in {
      val req = HttpRequest(HttpMethods.GET, Uri("/sharings"))
      val resp = ValidationError(Seq("Invalid"))
      val funMock = mock(classOf[PartialFunction[ApiRequest, Future[ApiResponse]]])
      when(controller.handleRequestAsync).thenReturn(funMock)
      when(funMock.isDefinedAt(ListRequest)).thenReturn(true)
      when(controller.handleRequestAsync(ListRequest)).thenReturn(Future.failed(resp))

      whenReady(handleAsync(req)) { resp =>
        assert(resp.status == StatusCodes.BadRequest)
      }
    }

    "should return 500 if something goes totally wrong" in {
      val req = HttpRequest(HttpMethods.GET, Uri("/sharings"))

      val funMock = mock(classOf[PartialFunction[ApiRequest, Future[ApiResponse]]])
      when(controller.handleRequestAsync).thenReturn(funMock)
      when(funMock.isDefinedAt(ListRequest)).thenReturn(true)
      when(controller.handleRequestAsync(ListRequest)).thenReturn(Future.failed(new Error("")))

      whenReady(handleAsync(req)) { resp =>
        assert(resp.status == StatusCodes.InternalServerError)
      }
    }

    "should successfully handle parser errors" in {
      val req = HttpRequest(HttpMethods.GET, Uri("/sharings"))

      val funMock = mock(classOf[PartialFunction[ApiRequest, Future[ApiResponse]]])
      when(controller.handleRequestAsync).thenReturn(funMock)
      when(funMock.isDefinedAt(ListRequest)).thenReturn(true)
      when(controller.handleRequestAsync(ListRequest)).thenReturn(Future.failed(DeserializationException("Parser error")))

      whenReady(handleAsync(req)) { resp =>
        assert(resp.status == StatusCodes.BadRequest)
      }
    }

  }
}
