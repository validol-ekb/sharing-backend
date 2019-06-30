package ekb.validol.sharing.backend.transport

import scala.concurrent.Future

trait ServerInstance {

  def shutdown(): Future[Unit]

}
