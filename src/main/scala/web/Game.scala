package web

import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import web.Protocol.{GameMessage, GridMessage, SudokuMessage, UpdateSudoku}

trait Game {
  def gameFlow(): Flow[GameMessage, GameMessage, Any]
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
          var lastGame = Option.empty[Array[Array[GridMessage]]]

          {
            case m@SudokuMessage(sudoku) =>
              lastGame = Some(sudoku)
              m :: Nil
            case UpdateSudoku() =>
              if (lastGame.isDefined) {
                SudokuMessage(lastGame.get) :: Nil
              } else {
                Nil
              }
            case x => x :: Nil
          }
        }
        //Bring elements too all materialized sources attach, meaning every element for every chat member
        .toMat(BroadcastHub.sink[GameMessage])(Keep.both)
        .run()

    val chatChannel: Flow[GameMessage, GameMessage, Any] = Flow.fromSinkAndSource(in, out)

    () =>
      Flow[GameMessage]

        /*
        Goal: Prepent a message with the current state of the sudoku
        Challenge: from where I can get the current state of sudoku

        prepend a message that is received for the game flow, the game flow receives the message and send a sudoku update to everyone
         */
        .prepend(Source.single(UpdateSudoku()))
        .via(chatChannel)
  }
}
