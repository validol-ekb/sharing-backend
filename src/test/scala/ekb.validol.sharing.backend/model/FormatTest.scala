package ekb.validol.sharing.backend.model

import java.util.UUID

import org.scalatest.{FreeSpec, Matchers}
import spray.json._
import Format._
import ekb.validol.sharing.backend.model.Protocol._
import ekb.validol.sharing.backend.model.Selection.{Cell, CellRange, Empty}

class FormatTest extends FreeSpec with Matchers{

  "Format" - {

    "should convert add sharings request" in {
      val json = """{"users":["foo@bar.com","example@gmail.com"],"selections":["HRReport!A1","Actuals!B1:B100","Assumptions","Dashboard!C1:C4"]}""".parseJson
      val req = json.convertTo[AddRequest]

      req shouldEqual AddRequest(
        users = Seq("foo@bar.com", "example@gmail.com"),
        selections = Map(
          SheetName.Assumptions -> Seq(Empty),
          SheetName.HRReport -> Seq(new Cell("A1")),
          SheetName.Actuals -> Seq(CellRange(new Cell("B1"), new Cell("B100"))),
          SheetName.Dashboard -> Seq(CellRange(new Cell("C1"), new Cell("C4")))))
    }

    "should group selection by sheet name" in {
      val json = """{"users":["foo@bar.com"], "selections":["Assumptions!A5:A11", "Assumptions!A1", "Assumptions!B8:B146"]}""".parseJson
      val req = json.convertTo[AddRequest]

      req shouldEqual AddRequest(
        users = Seq("foo@bar.com"),
        selections = Map(
          SheetName.Assumptions -> Seq(
            CellRange(new Cell("A5"), new Cell("A11")),
            new Cell("A1"),
            CellRange(new Cell("B8"), new Cell("B146"))))
      )
    }

    "should convert ListResponse to valid json" in {
      val uuid1 = UUID.randomUUID()
      val uuid2 = UUID.randomUUID()
      val response = ListResponse(
        sharings = Map(
          uuid1 -> Sharing(
            users = Seq("foo@bar.com"),
            selections = Map(SheetName.Assumptions -> Seq(Empty), SheetName.HRReport -> Seq(new Cell("A1")))),
          uuid2 -> Sharing(
            users = Seq("foo@bar.com", "example@gmail.com"),
            selections = Map(SheetName.Actuals -> Seq(CellRange(new Cell("B1"), new Cell("B100"))))
          )
        )
      )
      val json = response.toJson.compactPrint

      json shouldEqual s"""[{"id":"$uuid1","selections":["Assumptions","HRReport!A1"],"users":["foo@bar.com"]},{"id":"$uuid2","selections":["Actuals!B1:B100"],"users":["foo@bar.com","example@gmail.com"]}]"""
    }

    "should convert SuccessResponse to valid json" in {
      val uuid = UUID.randomUUID()
      val response = SuccessResponse(uuid)

      val json = response.toJson.compactPrint

      json shouldEqual s"""{"id":"$uuid"}"""
    }

    "should convert ErrorResponse to valid json" in {
      val err = ErrorResponse("Some error")
      val json = err.toJson.compactPrint

      json shouldEqual s"""{"errors":["${err.msg}"]}"""
    }

    "should convert ValidationError to valid json" in {
      val err = ValidationError(Seq("error1", "error2"))
      val json = err.toJson.compactPrint

      json shouldEqual s"""{"errors":["error1","error2"]}"""
    }

  }

}
