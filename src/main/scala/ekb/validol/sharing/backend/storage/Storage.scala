package ekb.validol.sharing.backend.storage


import scala.concurrent.Future

trait Storage[K, V] {

  def list: Future[Map[K, V]]

  def add(v: V): Future[K]

  def shutdown(): Future[Unit]
}
