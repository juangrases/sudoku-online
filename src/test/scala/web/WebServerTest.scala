package web

import org.scalatest.flatspec.AnyFlatSpec
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Framing, Keep, Sink, Source}
import akka.util.ByteString

import scala.io.StdIn




class WebServerTest extends AnyFlatSpec{


  "A source" should "do things" in {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()

    val source = Source.single(4)

    val sink = Sink.foreach[Int](elem => println(s"sink received: $elem"))


    val fromSinkAndSource = Flow.fromSinkAndSource(Sink.foreach[Int](elem => println(s"sink 3 received: $elem")),  Source.single(10))


    val perTwo = Flow[Int].map(_ * 2)
    val inverse = Flow[Int].map(_ * -1)

    val flowWithFlow = perTwo.via(inverse).via(fromSinkAndSource) //Applies the transformation on the source (4)
    val flowWithFlow2 = fromSinkAndSource.via(perTwo).via(inverse) //Applies the transformation on the  10

//    source.via(flowWithFlow).runWith(Sink.foreach[Int](elem => println(s"sink two received: $elem")))
//    source.via(flowWithFlow2).runWith(sink2)


    //Whatever is send to the sink, goes to the source


//    val x = fromSinkAndSource.runWith(Source.single("3"), sink2)


//    source.to(sink).run

  }
}
