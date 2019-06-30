package ekb.validol.sharing.backend.storage.impl

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, PoisonPill}
import ekb.validol.sharing.backend.model.Sharing
import ekb.validol.sharing.backend.storage.Storage
import ekb.validol.sharing.backend.storage.impl.InMemoryStorage._

import scala.concurrent.{ExecutionContext, Future}
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.Config
import ekb.validol.sharing.backend.storage.impl.InMemoryActorStorage.{AddResponse, ListResponse}

class InMemoryStorage(config: Config)(implicit system: ActorSystem, ec: ExecutionContext) extends Storage[StorageKey, StorageItem] {

  implicit val timeout = Timeout.apply(config.getDuration("query-timeout", TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS)
  private val storage = system.actorOf(InMemoryActorStorage.props)

  override def list: Future[Map[StorageKey, StorageItem]] = (storage ? InMemoryActorStorage.GetList).map {
    case lr: ListResponse => lr.data
  }

  override def add(v: StorageItem): Future[StorageKey] = (storage ? InMemoryActorStorage.AddItem(v)).map {
    case ar: AddResponse => ar.id
  }

  override def shutdown(): Future[Unit] = {
    storage ! PoisonPill
    Future.unit
  }

}

object InMemoryStorage {
  
  type StorageKey = UUID
  type StorageItem = Sharing

  def apply(config: Config)(implicit system: ActorSystem, ec: ExecutionContext): InMemoryStorage = new InMemoryStorage(config)

}
