package ekb.validol.sharing.backend.transport.impl.http

import akka.http.scaladsl.Http.ServerBinding
import ekb.validol.sharing.backend.transport.ServerInstance

import scala.concurrent.{ExecutionContext, Future}

class HttpServerInstance(binding: ServerBinding)(implicit ec: ExecutionContext) extends ServerInstance {

  override def shutdown(): Future[Unit] = {
    binding.unbind().map(_ => ())
  }

  override def toString: String = {
    s"host=${binding.localAddress.getHostString}; port=${binding.localAddress.getPort}"
  }

}

object HttpServerInstance {

  def apply(binding: ServerBinding)(implicit ec: ExecutionContext): HttpServerInstance = new HttpServerInstance(binding)

}
