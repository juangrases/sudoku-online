package web

import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl._
import sudoku.{SudokuHelper, Sudokus}
import web.Protocol.{ChangedGrid, GameMessage, MemberLeft, PollSudoku, Score, SudokuMessage}

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
          var lastGame = getRandomSudoku
          var scores = Map[String, Score]()

          val candidates = SudokuHelper.toSudokuWithCandidates(lastGame.map(_.map(v => if (v.value == "") None else Try(v.value.toInt).toOption)))
          val solvedSudoku = SudokuHelper.solveSudoku(candidates).get

          {
            case m@SudokuMessage(sudoku, member, Some(ChangedGrid(value, row, col)), _) =>
              if (solvedSudoku.v(row)(col).value.get == value.toInt) {

                lastGame = sudoku
                val previousScore = scores.get(member)
                scores = scores + (member -> previousScore.map(p => p.copy(successes = p.successes + 1)).getOrElse(Score(0, 1)))

                m.copy(scores = Some(scores)) :: Nil
              } else {
                val previousScore = scores.get(member)
                scores = scores + (member -> previousScore.map(p => p.copy(wrongs = p.wrongs + 1)).getOrElse(Score(1, 0)))
                SudokuMessage(lastGame, member, None, Some(scores)) :: Nil
              }
            case PollSudoku(member) =>
              scores = scores + (member -> Score(0,0))
              SudokuMessage(lastGame, member, None, Some(scores)) :: Nil
            case Protocol.MemberLeft(member) =>
              SudokuMessage(lastGame, member, None, Some(scores.removed(member))) :: Nil
            case x => x :: Nil
          }
        }
        .statefulMapConcat[GameMessage] { () =>
          var members = Set.empty[String]

          {
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
        //Allow new players that just joined to get latest Sudoku
        .prepend(Source.single(PollSudoku(member)))
        .concat(Source.single(MemberLeft(member)))
        .via(chatChannel)
  }

  private def getRandomSudoku = {
    val rand = new Random(System.currentTimeMillis())
    val random_index = rand.nextInt(Sudokus.hardGames.length)
    Sudokus.toSudokuMessage(Sudokus.hardGames(random_index))
  }
}
