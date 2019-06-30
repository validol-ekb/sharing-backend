package ekb.validol.sharing.backend

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import ekb.validol.sharing.backend.controller.impl.MainControllerImpl
import ekb.validol.sharing.backend.storage.impl.InMemoryStorage
import ekb.validol.sharing.backend.transport.impl.http.HttpServerAppImpl

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Boot extends App {

  implicit val system = ActorSystem("sharing-backend")
  val config = ConfigFactory.load()
  val apiConfig = config.getConfig("ekb.validol.sharing.backend.http")
  val dbConfig = config.getConfig("ekb.validol.sharing.backend.db")
  val transport = HttpServerAppImpl(apiConfig)
  val storage = InMemoryStorage(dbConfig)

  transport.addHandler(MainControllerImpl.apply(storage))

  transport.start().onComplete {
    case Success(binding) =>
      println(s"Server is running on $binding")
    case Failure(_) =>
      storage.shutdown()
      system.terminate()
  }


}
