package web

import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import sudoku.SudokuHelper
import web.Protocol.{GameMessage, GridMessage, MemberJoined, MemberLeft, PollSudoku, Score, SudokuMessage, WrongMove}

import scala.util.Try

trait Game {
  def gameFlow(user: String): Flow[GameMessage, GameMessage, Any]
}

object Game {

  val theGame = Array(
    Array(GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("8", false)),
    Array(GridMessage("7", false), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("4", false), GridMessage("", true), GridMessage("3", false), GridMessage("", true)),
    Array(GridMessage("", true), GridMessage("4", false), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("3", false), GridMessage("2", false), GridMessage("", true), GridMessage("", true)),
    Array(GridMessage("2", false), GridMessage("", true), GridMessage("", true), GridMessage("3", false), GridMessage("9", false), GridMessage("", true), GridMessage("8", false), GridMessage("", true), GridMessage("4", false)),
    Array(GridMessage("", true), GridMessage("", true), GridMessage("7", false), GridMessage("8", false), GridMessage("2", false), GridMessage("", true), GridMessage("", true), GridMessage("6", false), GridMessage("3", false)),
    Array(GridMessage("", true), GridMessage("5", false), GridMessage("", true), GridMessage("", true), GridMessage("7", false), GridMessage("6", false), GridMessage("", true), GridMessage("9", false), GridMessage("2", false)),
    Array(GridMessage("", true), GridMessage("7", false), GridMessage("4", false), GridMessage("2", false), GridMessage("6", false), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("", true)),
    Array(GridMessage("", true), GridMessage("3", false), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("", true), GridMessage("6", false), GridMessage("8", false), GridMessage("", true)),
    Array(GridMessage("5", false), GridMessage("", true), GridMessage("6", false), GridMessage("", true), GridMessage("", true), GridMessage("9", false), GridMessage("", true), GridMessage("", true), GridMessage("7", false))
  )

  def create()(implicit system: ActorMaterializer): Game = {
    /*
    This pattern allows to have a broadcast every message to everyone approach by configuring a Source from all
    the incoming web sockets, and a Sink for all the web sockets
     */
    val (in, out) =
      MergeHub.source[GameMessage]
        .statefulMapConcat[GameMessage] { () =>
          var lastGame = theGame
          var scores = Map[String, Score]()

          {
            case m@SudokuMessage(sudoku, Some(member), _) =>
              lastGame = sudoku
              val previousScore = scores.get(member)
              scores = scores + (member -> previousScore.map(p => p.copy(successes = p.successes + 1)).getOrElse(Score(0, 1)))
              m.copy(scores = Some(scores)) :: Nil
            case m@SudokuMessage(sudoku, None, _) =>
              lastGame = sudoku
              m.copy(scores = Some(scores)) :: Nil
            case PollSudoku() =>
              SudokuMessage(lastGame, None, Some(scores)) :: Nil

            case WrongMove(member) =>
              val previousScore = scores.get(member)
              scores = scores + (member -> previousScore.map(p => p.copy(wrongs = p.wrongs + 1)).getOrElse(Score(1, 0)))
              SudokuMessage(lastGame, Some(member), Some(scores)) :: Nil

            case x => x :: Nil
          }
        }
        .statefulMapConcat[GameMessage] { () =>
          var members = Set.empty[String]

          {
            case Protocol.MemberJoined(newMember) =>
              members += newMember
              Protocol.Members(members.toSeq) :: Nil
            case Protocol.MemberLeft(member) =>
              members -= member
              Protocol.Members(members.toSeq) :: Nil
            case x => x :: Nil
          }
        }
        //Bring elements too all materialized sources attach, meaning every element for every chat member
        .toMat(BroadcastHub.sink[GameMessage])(Keep.both)
        .run()

    val chatChannel: Flow[GameMessage, GameMessage, Any] = Flow.fromSinkAndSource(in, out)

    (member: String) =>
      Flow[GameMessage]
        .map {
          case m@SudokuMessage(sudoku, _, _) =>
            val candidates = SudokuHelper.toSudokuWithCandidates(sudoku.map(_.map(v => if (v.value == "") None else Try(v.value.toInt).toOption)))
            SudokuHelper.solveSudoku(candidates) match {
              case None =>
                WrongMove(member)
              case Some(_) =>
                m.copy(member=Some(member))
            }
          case m => m
        }
        //Allow new players that just joined to get latest Sudoku
        .prepend(Source.single(PollSudoku()))
        .prepend(Source.single(MemberJoined(member)))
        .concat(Source.single(MemberLeft(member)))
        .via(chatChannel)
  }
}
