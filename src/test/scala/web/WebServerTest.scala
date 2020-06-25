package web

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.Future




class WebServerTest extends AnyFlatSpec{


  "A source" should "do things" in {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()


    val source = Source(1 to 10)
    val sink = Sink.fold[Int, Int](0)(_ + _)

    // connect the Source to the Sink, obtaining a RunnableGraph
    val runnable = source.toMat(sink)(Keep.both)

    // materialize the flow and get the value of the FoldSink
//    val sum: Future[Int] = runnable.run()

  }
}
