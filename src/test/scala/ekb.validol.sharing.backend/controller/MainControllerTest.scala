package ekb.validol.sharing.backend.controller

import java.util.UUID

import ekb.validol.sharing.backend.controller.impl.MainControllerImpl
import ekb.validol.sharing.backend.model.Protocol._
import ekb.validol.sharing.backend.model.Selection.Cell
import ekb.validol.sharing.backend.model.{Sharing, SheetName}
import ekb.validol.sharing.backend.storage.Storage
import ekb.validol.sharing.backend.storage.impl.InMemoryStorage.{StorageItem, StorageKey}
import org.scalatest.{FreeSpec, Matchers}
import org.mockito.{Matchers, Mockito}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.FreeSpec
import org.scalatest.concurrent._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionException, Future}

class MainControllerTest extends FreeSpec with ScalaFutures {

  "MainController" - {

    "should correctly handle valid ListRequest" in {
      val storage = mock(classOf[Storage[StorageKey, StorageItem]])
      val controller = MainControllerImpl(storage)
      val uuid = UUID.randomUUID()
      val storageReturn = Map(uuid -> Sharing(Seq.empty, Map.empty))

      when(storage.list).thenReturn(Future.successful(storageReturn))
      whenReady(controller.handleRequestAsync(ListRequest)) { resp =>
        assert(resp == ListResponse(storageReturn))
        verify(storage, times(1)).list
      }
    }

    "should correctly handle valid AddRequest" in {
      val storage = mock(classOf[Storage[StorageKey, StorageItem]])
      val controller = MainControllerImpl(storage)
      val uuid = UUID.randomUUID()

      val req = AddRequest(Seq("foo@bar.com"), Map(SheetName.Actuals -> Seq(new Cell("B1"))))

      when(storage.add(Sharing(req.users, req.selections))).thenReturn(Future.successful(uuid))

      whenReady(controller.handleRequestAsync(req)) { resp =>
        assert(resp == SuccessResponse(uuid))
        verify(storage, times(1)).add(Sharing(req.users, req.selections))
      }
    }

    "should return an error for invalid AddRequest" in {
      val storage = mock(classOf[Storage[StorageKey, StorageItem]])
      val controller = MainControllerImpl(storage)

      val req = AddRequest(Seq("1121212"), Map.empty)

      whenReady(controller.handleRequestAsync(req).failed) { error =>
        assert(error.isInstanceOf[ValidationError])
        verify(storage, times(0)).add(Sharing(req.users, req.selections))
      }
    }

    "should return an error on Database error" in {
      val storage = mock(classOf[Storage[StorageKey, StorageItem]])
      val controller = MainControllerImpl(storage)
      val error = new Error("Database error")

      when(storage.list).thenReturn(Future.failed(error))

      whenReady(controller.handleRequestAsync(ListRequest).failed) { err =>
        assert(err.isInstanceOf[ExecutionException])
        verify(storage, times(1)).list
      }
    }

    "should return an error on empty AddRequest" in {
      val storage = mock(classOf[Storage[StorageKey, StorageItem]])
      val controller = MainControllerImpl(storage)

      val req = AddRequest(Seq.empty, Map.empty)

      whenReady(controller.handleRequestAsync(req).failed) { err =>
        assert(err.isInstanceOf[ValidationError])
        verify(storage, times(0)).add(Sharing(req.users, req.selections))
      }
    }

  }

}
