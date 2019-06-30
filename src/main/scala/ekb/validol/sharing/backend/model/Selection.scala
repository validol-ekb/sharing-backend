package ekb.validol.sharing.backend.model

sealed trait Selection {
  def isValid: Boolean
}

object Selection {

  case class Cell(letter: String, number: Int) extends Selection with Ordered[Cell] {

    def this(str: String) {
      this(str.filter(!_.isDigit), str.filter(_.isDigit).toInt)
    }

    override def compare(that: Cell): Int = {
      val letters = this.letter.compare(that.letter)
      if (letters == 0) {
        number.compare(that.number)
      } else letters
    }

    override def isValid: Boolean = number > 0
  }

  case class CellRange(leftCell: Cell, rightCell: Cell) extends Selection {

    def isValid: Boolean = leftCell < rightCell

  }

  case object Empty extends Selection {

    override val isValid: Boolean = true

  }

}