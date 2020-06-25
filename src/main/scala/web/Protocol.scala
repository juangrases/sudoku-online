package web

import play.api.libs.json.Json

object Protocol {
  sealed trait GameMessage
  case class GridMessage(value: String, editable: Boolean)
  case class SudokuMessage(sudoku: Array[Array[GridMessage]]) extends GameMessage
  case class UpdateSudoku() extends GameMessage
  case class WrongMove() extends GameMessage


  implicit val gridMessageFormat = Json.format[GridMessage]
  implicit val sudokuMessageFormat = Json.format[SudokuMessage]
}
