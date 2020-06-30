package web

import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import play.api.libs.json.{JsArray, Json}
import web.Protocol.{ChangedGrid, GridMessage, MemberJoined, NextTurn, GameState}

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

    var cancellable: Option[Cancellable] = None

    //For every browser websocket connection, a Flow is created
    def websocketGameFlow(name: String): Flow[Message, Message, Any] = {
      println("New connection from " + name)


      if(cancellable.isEmpty){
        cancellable = Some(
          system.scheduler.scheduleAtFixedRate(15 second, 15.second) { () =>
            theChat.injectMessage(NextTurn(None))
          }
        )
      }

      Flow[Message]
        .mapAsync(1) {
          case e: TextMessage =>
            e.toStrict(2 seconds)
          case e => Future.successful(e)
        }
        .collect {
          case TextMessage.Strict(msg) =>
            val jsonValue = Json.parse(msg)
            ChangedGrid(
              name,
              (jsonValue \ "value").as[String],
              (jsonValue \ "row").as[Int],
              (jsonValue \ "col").as[Int]
            )
        }
        .via(theChat.gameFlow(name)) // ... and route them through the chatFlow ...
        //Create a single game state message
        .collect {
          case m: Protocol.GameState =>
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
