package web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import play.api.libs.json.{JsArray, Json}
import web.Protocol.{ChangedGrid, GridMessage, SudokuMessage}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.Failure

object WebServer {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = Materializer(system)
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher
    val defaultSettings = ServerSettings(system)
    val websocketSettings = defaultSettings.websocketSettings.withPeriodicKeepAliveMaxIdle(1 second)
    val customSettings = defaultSettings.withWebsocketSettings(websocketSettings)
    val theChat = Game.create()

    //For every browser websocket connection, a Flow is created
    def websocketGameFlow(name: String): Flow[Message, Message, Any] = {
      println("New connection from " + name)
      /*
      When first person joins
       */
      Flow[Message]
        .mapAsync(1) {
          case e: TextMessage =>
            e.toStrict(2 seconds)
          case e => Future.successful(e)
        }
        .collect {
          case TextMessage.Strict(msg) =>
           val jsonValue = Json.parse(msg)
            SudokuMessage(
              (jsonValue \ "sudoku").as[Array[Array[GridMessage]]],
              name,
              (jsonValue \ "changedGrid").asOpt[ChangedGrid],
              None
            )
        }
        .via(theChat.gameFlow(name)) // ... and route them through the chatFlow ...
        .collect {
          case m: Protocol.SudokuMessage =>
            TextMessage.Strict(Json.stringify(Json.toJson(m))) // ... pack outgoing messages into WS JSON messages ...
          case m: Protocol.Members =>
            TextMessage.Strict(Json.stringify(Json.toJson(m)))
        }
        .via(reportErrorsFlow) // ... then log any processing errors on stdin
    }

    def reportErrorsFlow[T]: Flow[T, T, Any] =
      Flow[T]
        .watchTermination()((_, f) => f.onComplete {
          case Failure(cause) =>
            println(s"WS stream failed with $cause")
          case _ => // ignore regular completion
            println("connection terminated")
        })

    val route =
      concat(
        pathPrefix("public") {
          extractUnmatchedPath { remaining =>
            getFromFile(s"src/main/public$remaining")
          }
        },
        extractUnmatchedPath { remaining =>
          getFromFile(s"sudoku-frontend/build" + remaining)
        },
        path("game") {
          parameter("name") { name =>
            handleWebSocketMessages(websocketGameFlow(name))
          }
        },
        getFromFile(s"sudoku-frontend/build/index.html")
      )

    val bindingFuture = Http().bindAndHandle(route, "192.168.1.16", 8080, settings = customSettings)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
