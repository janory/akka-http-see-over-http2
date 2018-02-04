package io.janory

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}
import java.time.LocalTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import javax.net.ssl.{KeyManagerFactory, SSLContext}

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.{Http, HttpsConnectionContext}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor._
import java.io.File
import java.util.UUID

import scala.util.Random
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._


object HelloServerHttp2WithSse {

  val ExampleServerContext = {
    // never put passwords into code!
    val password = "changeit".toCharArray

    val ks = KeyStore.getInstance("PKCS12")
    ks.load(resourceStream("server.p12"), password)

    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, null, new SecureRandom)

    new HttpsConnectionContext(context)
  }

  def resourceStream(resourceName: String): InputStream = {
    val is = getClass.getClassLoader.getResourceAsStream(resourceName)
    require(is ne null, s"Resource $resourceName not found")
    is
  }

  def main(args: Array[String]) {
    import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._

    implicit val actorSystem       = ActorSystem("system")
    implicit val actorMaterializer = ActorMaterializer()

    import actorSystem.dispatcher

    val overflowStrategy = akka.stream.OverflowStrategy.dropHead

    import akka.stream.scaladsl.SourceQueueWithComplete

    val promise = Promise[SourceQueueWithComplete[String]]()

//    val source: Source[ServerSentEvent, SourceQueueWithComplete[String]] =
    val source: ToResponseMarshallable = Source
      .queue[String](100, overflowStrategy)
//      .map(_ => LocalTime.now())
      .map(state => ServerSentEvent(state))
      .mapMaterializedValue((queue: SourceQueueWithComplete[String]) => promise.trySuccess(queue))
    //      .keepAlive(1.second, () => ServerSentEvent.heartbeat)

    val testActor = actorSystem.actorOf(Props(new Actor {
      var connections = Map.empty[String, ActorRef]

      override def receive: Receive = {
        case (connection: String, ref: ActorRef) => {
          println(s"$connection connection added!")
          connections += connection -> ref
        }
        case message: String => {
          println(s"got new message: $message")
          connections.values.foreach(_ ! message)
        }
      }
    }))


    val b: ActorRef => Unit = testActor ! (UUID.randomUUID.toString, _)
    val c: ActorRef => Unit = testActor.tell(UUID.randomUUID.toString, _)
    val d: ActorRef => Unit = (ref: ActorRef) => testActor.tell((UUID.randomUUID.toString, ref), ref)


      val test: ToResponseMarshallable = Source.actorRef[String](100, OverflowStrategy.fail)
        .mapMaterializedValue(d)
        .map(msg => ServerSentEvent(msg))
//        .mapMaterializedValue(ref => testActor.tell(UUID.randomUUID.toString,ref)).map(msg => ServerSentEvent(msg))
//        .mapMaterializedValue(testActor.tell("created",_)).map(msg => ServerSentEvent(msg))


    val syncHandler: HttpRequest => HttpResponse = {
      case HttpRequest(GET, Uri.Path("/on"), _, _, _) => {
//        promise.future.map((t: SourceQueueWithComplete[String]) => t.offer("on"))
        testActor ! "on"

        HttpResponse()
      }
      case HttpRequest(GET, Uri.Path("/off"), _, _, _) => {
//        promise.future.map((t: SourceQueueWithComplete[String]) => t.offer("off"))
        testActor ! "off"

        HttpResponse()
      }
    }

    val asyncHandler: HttpRequest => Future[HttpResponse] = {
      case req @ HttpRequest(GET, Uri.Path("/events"), _, _, _) => {
        test(req)
      }
      case req => Future.successful(syncHandler(req))

    }

    Http().bindAndHandleAsync(asyncHandler,
                              interface = "0.0.0.0",
                              port = 8443,
                              ExampleServerContext)

    println("http2 server started at 8443")
  }
}
