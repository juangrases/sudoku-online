package web

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, UpgradeToWebSocket}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

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
    // This flow received a Message from client and transform it to a new Message respond to the client
    def websocketChatFlow(): Flow[Message, Message, Any] =
      Flow[Message]
//        //Collect allows to filter and map in one operation
//        .collect {
//          case TextMessage.Strict(msg) => msg // unpack incoming WS text messages...
//          // This will lose (ignore) messages not received in one chunk (which is
//          // unlikely because chat messages are small) but absolutely possible
//          // FIXME: We need to handle TextMessage.Streamed as well.
//        }
        //This line joins this Flow to the Chat Flow which goes from String to Protocol
        .via(theChat.gameFlow()) // ... and route them through the chatFlow ...
//        .map {
//          case msg: Protocol.ChatMessage =>
//            TextMessage.Strict(msg.message)
//          case msg: Protocol.Message =>
//            TextMessage.Strict(msg.toString) // ... pack outgoing messages into WS JSON messages ...
//        }
          .via(reportErrorsFlow) // ... then log any processing errors on stdin

    def reportErrorsFlow[T]: Flow[T, T, Any] =
      Flow[T]
        .watchTermination()((_, f) => f.onComplete {
          case Failure(cause) =>
            println(s"WS stream failed with $cause")
          case _ => // ignore regular completion
        })

    val greeterWebSocketService =
      Flow[Message]
        .mapConcat {
          // we match but don't actually consume the text message here,
          // rather we simply stream it back as the tail of the response
          // this means we might start sending the response even before the
          // end of the incoming message has been received
          case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
          case bm: BinaryMessage =>
            // ignore binary messages but drain content to avoid the stream being clogged
            bm.dataStream.runWith(Sink.ignore)
            Nil
        }

//    val requestHandler: HttpRequest => HttpResponse = {
//      case req @ HttpRequest(HttpMethods.GET, Uri.Path("/greeter"), _, _, _) =>
//        req.header[UpgradeToWebSocket] match {
//          case Some(upgrade) => upgrade.handleMessages(greeterWebSocketService)
//          case None          => HttpResponse(400, entity = "Not a valid websocket request!")
//        }
//
//      case r: HttpRequest =>
//        r.discardEntityBytes() // important to drain incoming HTTP Entity stream
//        HttpResponse(404, entity = "Unknown resource!")
//    }

    val route =
      concat(
        path("examples") {
          getFromBrowseableDirectories("src/main/public")
        },
        path("hello") {
          get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
          }
        },
        pathPrefix("public") {
          extractUnmatchedPath { remaining =>
            getFromFile(s"src/main/public$remaining")
          }
        },
        path("stream") {
          handleWebSocketMessages(greeterWebSocketService)
        },
        path("chat") {
          handleWebSocketMessages(websocketChatFlow())
        }
      )

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
//    val bindingFuture2 = Http().bindAndHandleSync(requestHandler, "localhost", 8081)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
