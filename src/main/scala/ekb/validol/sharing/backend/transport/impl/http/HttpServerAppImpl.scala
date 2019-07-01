package ekb.validol.sharing.backend.transport.impl.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import ekb.validol.sharing.backend.transport.{ServerApp, ServerInstance}

import scala.concurrent.{ExecutionContext, Future}

class HttpServerAppImpl(config: Config)(implicit system: ActorSystem, val ec: ExecutionContext) extends HttpRequestHandler
  with ServerApp[HttpRequest, HttpResponse] {

  implicit protected val materializer = ActorMaterializer()

  override def start(): Future[ServerInstance] = {
    Http()
      .bindAndHandleAsync(handleAsync, config.getString("host"), config.getInt("port"))
      .map(HttpServerInstance.apply)
  }

}

object HttpServerAppImpl {

  def apply(config: Config)(implicit system: ActorSystem, ec: ExecutionContext): HttpServerAppImpl = new HttpServerAppImpl(config)

}
