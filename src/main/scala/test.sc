import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}


implicit val system = ActorSystem("my-system")
implicit val materializer = ActorMaterializer()


val source = Source.single(4)

val sink = Sink.foreach[Int](elem => println(s"sink received: $elem"))

source.to(sink).run