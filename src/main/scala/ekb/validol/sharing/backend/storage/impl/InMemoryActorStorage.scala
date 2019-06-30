package ekb.validol.sharing.backend.storage.impl

import java.util.UUID

import akka.actor.{Actor, Props}
import ekb.validol.sharing.backend.storage.impl.InMemoryActorStorage.{AddItem, AddResponse, GetList, ListResponse}
import ekb.validol.sharing.backend.storage.impl.InMemoryStorage.{StorageItem, StorageKey}

import scala.collection.mutable

class InMemoryActorStorage extends Actor {

  private val storage = mutable.AnyRefMap.empty[StorageKey, StorageItem]

  override def receive: Receive = {
    case GetList =>
      sender() ! ListResponse(storage.toMap)
    case AddItem(v) =>
      val id = generateId()
      storage += (id -> v)
      sender() ! AddResponse(id)
  }

  private def generateId(): StorageKey = UUID.randomUUID()
}

object InMemoryActorStorage {

  def props: Props = Props(new InMemoryActorStorage)

  case object GetList
  case class AddItem(v: StorageItem)

  case class ListResponse(data: Map[StorageKey, StorageItem])
  case class AddResponse(id: UUID)

}
