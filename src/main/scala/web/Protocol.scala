package web

import play.api.libs.json.Json

object Protocol {
  sealed trait GameMessage
  case class GridMessage(value: String, editable: Boolean)
  case class SudokuMessage(sudoku: Array[Array[GridMessage]], member: String, scores: Option[Map[String, Score]]) extends GameMessage
  case class PollSudoku(member: String) extends GameMessage
  case class WrongMove(member: String) extends GameMessage
  case class MemberJoined(member: String) extends GameMessage
  case class MemberLeft(member: String) extends GameMessage
  case class Members(allMembers: Seq[String]) extends GameMessage
  case class Score(wrongs: Int, successes: Int) extends GameMessage


  implicit val gridMessageFormat = Json.format[GridMessage]
  implicit val scoreFormat = Json.format[Score]
  implicit val sudokuMessageFormat = Json.format[SudokuMessage]
  implicit val membersFormat = Json.format[Members]
}
