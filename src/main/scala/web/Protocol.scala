package web

import play.api.libs.json.Json

object Protocol {
  sealed trait GameMessage


  case class GridMessage(value: String, editable: Boolean)
  case class Score(wrongs: Int, successes: Int)

  case class GameState(sudoku: Array[Array[GridMessage]],
                       currentTurn: Option[String],
                       lastStartTime: Option[Long],
                       scores: Map[String, Score],
                       turnDuration: Int) extends GameMessage


  case class ChangedGrid(member: String, value: String, row: Int, col: Int) extends GameMessage
  case class MemberJoined(member: String) extends GameMessage
  case class MemberLeft(member: String) extends GameMessage
  case class NextTurn() extends GameMessage


  implicit val gridMessageFormat = Json.format[GridMessage]
  implicit val changedGridFormat = Json.format[ChangedGrid]
  implicit val scoreFormat = Json.format[Score]
  implicit val sudokuMessageFormat = Json.format[GameState]
}
