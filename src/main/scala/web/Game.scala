package web

import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl._
import sudoku.{SudokuHelper, Sudokus}
import web.Protocol.{GameMessage, GridMessage, MemberJoined, MemberLeft, PollSudoku, Score, SudokuMessage, WrongMove}

import scala.util.{Random, Try}

trait Game {
  def gameFlow(user: String): Flow[GameMessage, GameMessage, Any]
}

object Game {

  def create()(implicit system: Materializer): Game = {
    /*
    This pattern allows to have a broadcast every message to everyone approach by configuring a Source from all
    the incoming web sockets, and a Sink for all the web sockets
     */
    val (in, out) =
      MergeHub.source[GameMessage]
        .statefulMapConcat[GameMessage] { () =>
          val rand = new Random(System.currentTimeMillis())
          val random_index = rand.nextInt(Sudokus.hardGames.length)
          var lastGame = Sudokus.toSudokuMessage(Sudokus.hardGames(random_index))
          var scores = Map[String, Score]()

          {
            case m@SudokuMessage(sudoku, member, _) =>
              val updateScore = lastGame.flatten.count(_.value != "") < sudoku.flatten.count(_.value != "")

              lastGame = sudoku
              val previousScore = scores.get(member)
              if(updateScore){
                scores = scores + (member -> previousScore.map(p => p.copy(successes = p.successes + 1)).getOrElse(Score(0, 1)))
              }
              m.copy(scores = Some(scores)) :: Nil
            case PollSudoku(member) =>
              SudokuMessage(lastGame, member, Some(scores)) :: Nil

            case WrongMove(member) =>
              val previousScore = scores.get(member)
              scores = scores + (member -> previousScore.map(p => p.copy(wrongs = p.wrongs + 1)).getOrElse(Score(1, 0)))
              SudokuMessage(lastGame, member, Some(scores)) :: Nil

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
                m
            }
          case m => m
        }
        //Allow new players that just joined to get latest Sudoku
        .prepend(Source.single(PollSudoku(member)))
        .prepend(Source.single(MemberJoined(member)))
        .concat(Source.single(MemberLeft(member)))
        .via(chatChannel)
  }
}
