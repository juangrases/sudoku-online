package web

/*
 From https://github.com/jrudolph/akka-http-scala-js-websocket-chat
 */

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{Materializer, OverflowStrategy}
import sudoku.{SudokuHelper, Sudokus}
import web.Protocol.{GameMessage, GameState, GridMessage, MemberJoined, MemberLeft, NextTurn, Score}

import scala.util.{Random, Try}
import scala.concurrent.duration._

trait Game {
  def gameFlow(member: String): Flow[GameMessage, GameMessage, Any]

  def injectMessage(message: Protocol.GameMessage): Unit
}

object Game {

  def create()(implicit system: ActorSystem): Game = {
    implicit val executionContext = system.dispatcher
    /*
    This pattern allows to have a broadcast every message to everyone approach by configuring a Source from all
    the incoming web sockets, and a Sink for all the web sockets
     */
    val ((in, injectionQueue), out) =
      MergeHub.source[GameMessage]
        .mergeMat(Source.queue[Protocol.GameMessage](100, OverflowStrategy.dropNew))(Keep.both)
        .statefulMapConcat[GameMessage] { () =>
          val lastGame = getRandomSudoku
          var scores = Map[String, Score]()
          var currentTurnPos = 0
          var currentTurn: Option[String] = None
          var lastTypeStarted: Option[Long] = None

          val candidates = SudokuHelper.toSudokuWithCandidates(lastGame.map(_.map(v => if (v.value == "") None else Try(v.value.toInt).toOption)))
          val solvedSudoku = SudokuHelper.solveSudoku(candidates).get


          {
            case Protocol.ChangedGrid(member, value, row, col) =>
              val previousScore = scores.get(member)
              if (solvedSudoku.v(row)(col).value.get == Try(value.toInt).getOrElse(0)) {
                lastGame(row)(col) = GridMessage(value, true)
                scores = scores + (member -> previousScore.map(p => p.copy(successes = p.successes + 1)).getOrElse(Score(0, 1)))

                GameState(lastGame, currentTurn, lastTypeStarted, scores) :: Nil
              } else {
                scores = scores + (member -> previousScore.map(p => p.copy(wrongs = p.wrongs + 1)).getOrElse(Score(1, 0)))
                val currentMembers = scores.keys.toArray
                currentTurnPos = (currentTurnPos + 1) % currentMembers.length
                currentTurn = Some(currentMembers(currentTurnPos))
                lastTypeStarted=Some(System.currentTimeMillis())
                println(s"new turn is for $currentTurn")
                NextTurn() :: GameState(lastGame, currentTurn, lastTypeStarted, scores) :: Nil
              }
            case Protocol.MemberJoined(member) =>
              scores = scores + (member -> Score(0, 0))
              //If no current turn yet, set it for the user that just joined
              currentTurn = Option(currentTurn.getOrElse(member))
              lastTypeStarted=Option(lastTypeStarted.getOrElse(System.currentTimeMillis()))

              NextTurn() :: GameState(lastGame, currentTurn, lastTypeStarted,  scores) :: Nil

            case a@NextTurn() =>
              //Update the current turn to the next turn
              println("receiving new turn")
              val currentMembers = scores.keys.toArray
              if(currentMembers.isEmpty){
                currentTurn = None
              }else{
                currentTurnPos = (currentTurnPos + 1) % currentMembers.length
                currentTurn = Some(currentMembers(currentTurnPos))
                println(s"new turn is for $currentTurn")
                lastTypeStarted=Some(System.currentTimeMillis())
              }
              a :: GameState(lastGame, currentTurn, lastTypeStarted, scores) :: Nil

            case Protocol.MemberLeft(member) =>
              scores = scores.removed(member)
              GameState(lastGame, currentTurn, lastTypeStarted, scores) :: Nil

            case x => x :: Nil
          }
        }

        //Bring elements too all materialized sources attach, meaning every element for every chat member
        .toMat(BroadcastHub.sink[GameMessage])(Keep.both)
        .run()

    val s = Flow[GameMessage].mapConcat {
      case a@NextTurn() =>
        println("Next turn on the other sink")
        system.scheduler.scheduleOnce(45.second) { () =>
          injectionQueue.offer(NextTurn())
        }
        Nil
      case a => a :: Nil
    }

//    out.runWith(s)

    val chatChannel: Flow[GameMessage, GameMessage, Any] = Flow.fromSinkAndSource(in, out.via(s))
    new Game {


      //Executed every time a user connects to the game
      override def gameFlow(member: String): Flow[GameMessage, GameMessage, Any] =
        Flow[GameMessage]
          //Allow new players that just joined to get latest Sudoku
          .prepend(Source.single(MemberJoined(member)))
          .concat(Source.single(MemberLeft(member)))
          .via(chatChannel)

      override def injectMessage(message: Protocol.GameMessage): Unit = injectionQueue.offer(message)
    }
  }

  private def getRandomSudoku = {
    val rand = new Random(System.currentTimeMillis())
    val random_index = rand.nextInt(Sudokus.hardGames.length)
    Sudokus.toSudokuMessage(Sudokus.hardGames(random_index))
  }
}
