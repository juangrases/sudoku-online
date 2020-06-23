package web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

import scala.io.StdIn
import scala.util.Failure

object WebServer {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher


    val theChat = Game.create()

    //For every browser websocket connection, a Flow is created
    def websocketGameFlow(): Flow[Message, Message, Any] =
      //TODO: extract Json, validate sudoku is still valid, if so, continue, if not, fail
      Flow[Message]
        .via(theChat.gameFlow()) // ... and route them through the chatFlow ...
          .via(reportErrorsFlow) // ... then log any processing errors on stdin

    def reportErrorsFlow[T]: Flow[T, T, Any] =
      Flow[T]
        .watchTermination()((_, f) => f.onComplete {
          case Failure(cause) =>
            println(s"WS stream failed with $cause")
          case _ => // ignore regular completion
        })

    val route =
      concat(
        pathPrefix("public") {
          extractUnmatchedPath { remaining =>
            getFromFile(s"src/main/public$remaining")
          }
        },
        path("game") {
          handleWebSocketMessages(websocketGameFlow())
        }
      )

    val bindingFuture = Http().bindAndHandle(route, "192.168.1.16", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
