package ekb.validol.sharing.backend.model

import java.util.UUID

import ekb.validol.sharing.backend.model.Protocol._
import ekb.validol.sharing.backend.model.Selection.{Cell, CellRange, Empty}
import spray.json._

object Format extends DefaultJsonProtocol {

  implicit object Selection extends JsonReader[(SheetName.Value, Selection)] {
    override def read(json: JsValue): (SheetName.Value, Selection) = json match {
      case JsString(str) => str.split("!") match {
        case Array(v1, v2) => SheetName.withName(v1) -> (v2.split(":") match {
          case Array(c1, c2) => CellRange(new Cell(c1), new Cell(c2))
          case Array(c1) => new Cell(c1)
          case _ => deserializationError("Unknown request format")
        })
        case Array(v1) => SheetName.withName(v1) -> Empty
        case _ => deserializationError("Unknown request format")
      }
      case _ => deserializationError("Unknown request format")
    }
  }

  implicit object AddRequestProtocol extends JsonReader[AddRequest] {
    override def read(json: JsValue): AddRequest = json match {
      case obj: JsObject => AddRequest(
        users = obj.fields("users") match {
          case arr: JsArray => arr.elements.map(_.convertTo[String])
          case _ => deserializationError("Unknown request format")
        },
        selections = obj.fields("selections") match {
          case arr: JsArray =>
            arr.elements.map(_.convertTo[(SheetName.Value, Selection)])
              .groupBy(_._1)
              .map { case (k, v) =>
                k -> v.map(_._2).filter(_.isValid)
              }
          case _ => deserializationError("Unknown request format")
        }
      )
      case _ => deserializationError("Unknown request format")
    }
  }

  implicit object SelectionRowProtocol extends JsonWriter[(SheetName.Value, Selection)] {
    override def write(obj: (SheetName.Value, Selection)): JsValue = {
      obj._2 match {
        case c: Cell => JsString(s"${obj._1}!${c.toString}")
        case r: CellRange => JsString(s"${obj._1}!${r.leftCell}:${r.rightCell}")
        case Empty => JsString(s"${obj._1}")
      }
    }
  }

  implicit object SharingProtocol extends JsonWriter[(UUID, Sharing)] {
    override def write(obj: (UUID, Sharing)): JsValue = {
      val selections = obj._2.selections.map { case (k, v) =>
        v.map(d => k -> d)
      }.flatten

      JsObject(
        "id" -> JsString(obj._1.toString),
        "users" -> JsArray(obj._2.users.map(_.toJson).toVector),
        "selections" -> JsArray(selections.map(_.toJson).toVector)
      )
    }
  }

  implicit object ListResponseProtocol extends JsonWriter[ListResponse] {
    override def write(obj: ListResponse): JsValue = {
      JsArray(obj.sharings.map(_.toJson).toVector)
    }
  }

  implicit object ValidationResponseProtocol extends JsonWriter[ValidationError] {
    override def write(obj: ValidationError): JsValue = {
      JsObject(
        "errors" -> JsArray(obj.errors.map(JsString.apply).toVector)
      )
    }
  }

  implicit object SuccessResponseProtocol extends JsonWriter[SuccessResponse] {
    override def write(obj: SuccessResponse): JsValue = {
      JsObject("id" -> JsString(obj.id.toString))
    }
  }

  implicit object ErrorResponseProtocol extends JsonWriter[ErrorResponse] {
    override def write(obj: ErrorResponse): JsValue = {
      JsObject("errors" -> JsArray(JsString(obj.msg)))
    }
  }

}
