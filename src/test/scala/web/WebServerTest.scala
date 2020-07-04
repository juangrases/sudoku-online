package web

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Merge, RunnableGraph, Sink, Source}
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

    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      val in = Source(1 to 10)
      val out = Sink.foreach[Int](s => println(s))

      val bcast = builder.add(Broadcast[Int](2))
      val merge = builder.add(Merge[Int](2))

      val f1, f2, f3, f4,d,f,g = Flow[Int].map(_ + 10)

      in ~> f1 ~> bcast ~> f1 ~> merge ~> f1 ~> out
      bcast ~> f1 ~> merge
      ClosedShape
    })

    g.run()

  }
}
