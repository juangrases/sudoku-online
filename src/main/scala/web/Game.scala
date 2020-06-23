package web

import akka.http.scaladsl.model.ws.Message
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.util.control.NonFatal

trait Game {
  def gameFlow(): Flow[Message, Message, Any]
}

object Game {
  /*
  Many flows connect to the same flow (the chatChannel flow).
   This flows Broadcast the message that received to all other flows
   */
  def create()(implicit system: ActorMaterializer): Game = {
    // The chat room implementation
    val (in, out) =
    // Receives every element that the materialized sink received, meaning every element for every chat member
      MergeHub.source[Message]
        //Bring elements too all materialized sources attach, meaning every element for every chat member
        .toMat(BroadcastHub.sink[Message])(Keep.both)
        .run()

    // this flow gets messages from all the clients and send them to all flows
    val chatChannel: Flow[Message, Message, Any] = Flow.fromSinkAndSource(in, out)

    () => Flow[Message]
      // now wrap them in ChatMessages
//      .map(Protocol.ChatMessage(sender, _))
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
