package ekb.validol.sharing.backend.model

import ekb.validol.sharing.backend.model.Selection.{Cell, CellRange}
import org.scalatest.{FreeSpec, Matchers}

class SelectionTest extends FreeSpec with Matchers {

  "Cell" - {

    "should create a new instance from valid string" in {
      val cell = new Cell("B1")
      assert(cell.isValid)
    }

    "should create a new instance from invalid string" in {
      val cell = new Cell("B0")
      assert(!cell.isValid)
    }

    "should be in the list in right order" in {
      val cell1 = new Cell("B1")
      val cell2 = new Cell("B10")
      val cell3 = new Cell("C35")

      val list = List(cell2, cell1, cell3).sorted

      list should contain theSameElementsInOrderAs Seq(cell1, cell2, cell3)
    }

  }

  "Range" - {

    "should create instance from valid string" in {
      val cell1 = new Cell("B1")
      val cell2 = new Cell("B10")
      val range = CellRange(cell1, cell2)

      assert(range.isValid)
    }

    "should create instance from invalid string" in {
      val cell1 = new Cell("B1")
      val cell2 = new Cell("B10")
      val range = CellRange(cell2, cell1)

      assert(!range.isValid)
    }

  }

}
