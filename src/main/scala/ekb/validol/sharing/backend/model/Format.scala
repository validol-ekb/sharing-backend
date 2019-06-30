package ekb.validol.sharing.backend.model

import java.util.UUID

import ekb.validol.sharing.backend.model.Protocol.{AddRequest, ListResponse}
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
        selections = obj.fields("sections") match {
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

  implicit object SelectionProtocol extends JsonWriter[Selection] {
    override def write(obj: Selection): JsValue = obj match {
      case c: Cell => cellToJson(c)
      case r: CellRange => JsString(s"!${cellToJson(r.leftCell)}:${cellToJson(r.rightCell)}")
      case Empty => JsString("")
    }

    private def cellToJson(c: Cell): JsValue = {
      JsString(s"!${c.letter}${c.number}")
    }
  }

  implicit object SelectionRowProtocol extends JsonWriter[(SheetName.Value, Seq[Selection])] {
    override def write(obj: (SheetName.Value, Seq[Selection])): JsValue = {
      val items = obj._2.map { s =>
        JsString(s"${obj._1}${s.toJson}")
      }
      JsArray(items.toVector)
    }
  }

  implicit object SharingProtocol extends JsonWriter[(UUID, Sharing)] {
    override def write(obj: (UUID, Sharing)): JsValue = {
      JsObject(
        "id" -> JsString(obj._1.toString),
        "users" -> JsArray(obj._2.users.map(_.toJson).toVector),
        "selections" -> JsArray(obj._2.selections.map(_.toJson).toVector)
      )
    }
  }

  implicit object ListResponseProtocol extends JsonWriter[ListResponse] {
    override def write(obj: ListResponse): JsValue = {
      JsArray(obj.sharings.map(_.toJson).toVector)
    }
  }

}
