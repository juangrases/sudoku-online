package web

import play.api.libs.json.Json

object Protocol {
  sealed trait GameMessage
  case class GridMessage(value: String, editable: Boolean)
  case class SudokuMessage(sudoku: Array[Array[GridMessage]]) extends GameMessage
  case class UpdateSudoku() extends GameMessage
  case class WrongMove() extends GameMessage
  case class Joined(member: String, allMembers: Seq[String]) extends GameMessage


  implicit val gridMessageFormat = Json.format[GridMessage]
  implicit val sudokuMessageFormat = Json.format[SudokuMessage]
  implicit val joinedMessageFormat = Json.format[Joined]
}
