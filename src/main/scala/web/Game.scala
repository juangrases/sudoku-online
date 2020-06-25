package web

import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import sudoku.SudokuHelper
import web.Protocol.{GameMessage, GridMessage, Joined, SudokuMessage, PollSudoku, WrongMove}

import scala.util.Try

trait Game {
  def gameFlow(user: String): Flow[GameMessage, GameMessage, Any]
}

object Game {

  def create()(implicit system: ActorMaterializer): Game = {
    /*
    This pattern allows to have a broadcast every message to everyone approach by configuring a Source from all
    the incoming web sockets, and a Sink for all the web sockets
     */
    val (in, out) =
      MergeHub.source[GameMessage]
        .statefulMapConcat[GameMessage] { () =>
          var lastGame = Array(
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

          {
            case m@SudokuMessage(sudoku) =>
              lastGame = sudoku
              m :: Nil
            case PollSudoku() => SudokuMessage(lastGame) :: Nil

            case WrongMove() =>
              SudokuMessage(lastGame) :: Nil

            case x => x :: Nil
          }
        }
        .statefulMapConcat[GameMessage] { () =>
          var members = Set.empty[String]

          {
            case Protocol.Joined(newMember, _) =>
              members += newMember
              Protocol.Joined(newMember, members.toSeq) :: Nil
            case x => x :: Nil
          }
        }
        //Bring elements too all materialized sources attach, meaning every element for every chat member
        .toMat(BroadcastHub.sink[GameMessage])(Keep.both)
        .run()

    val chatChannel: Flow[GameMessage, GameMessage, Any] = Flow.fromSinkAndSource(in, out)

    (user: String) =>
      Flow[GameMessage]

        /*
        Goal: Prepent a message with the current state of the sudoku
        Challenge: from where I can get the current state of sudoku

        prepend a message that is received for the game flow, the game flow receives the message and send a sudoku update to everyone
         */
        //Check if new sudoku is valid
        .map {
          case m@SudokuMessage(sudoku) =>
            val candidates = SudokuHelper.toSudokuWithCandidates(sudoku.map(_.map(v => if (v.value == "") None else Try(v.value.toInt).toOption)))
            SudokuHelper.solveSudoku(candidates) match {
              case None =>
                WrongMove()
              case Some(s) =>
                m
            }
          case m => m
        }
        //Allow new players that just joined to get latest Sudoku
        .prepend(Source.single(PollSudoku()))
        .prepend(Source.single(Joined(user, Nil)))
        .via(chatChannel)
  }
}
