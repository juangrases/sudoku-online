package web

import akka.http.scaladsl.model.ws.Message
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

trait Game {
  def gameFlow(): Flow[Message, Message, Any]
}

object Game {

  def create()(implicit system: ActorMaterializer): Game = {
    /*
    This pattern allows to have a broadcast every message to everyone approach by configuring a Source from all
    the incoming web sockets, and a Sink for all the web sockets
     */
    val (in, out) =
      MergeHub.source[Message]
        //Bring elements too all materialized sources attach, meaning every element for every chat member
        .toMat(BroadcastHub.sink[Message])(Keep.both)
        .run()

    val chatChannel: Flow[Message, Message, Any] = Flow.fromSinkAndSource(in, out)

    () => Flow[Message]
      // and enclose them in the stream with Joined and Left messages
//      .prepend(Source.single(Protocol.Joined(sender, Nil)))
//      .concat(Source.single(Protocol.Left(sender, Nil)))
//      .recoverWithRetries(0, {
//        case NonFatal(ex) => Source(
//          Protocol.ChatMessage(sender, s"Oops, I crashed with $ex") ::
//            Protocol.Left(sender, Nil) :: Nil)
//      })
      .via(chatChannel)
  }
}
