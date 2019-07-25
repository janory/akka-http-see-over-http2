package io.janory

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext}

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpsConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer

import scala.concurrent.Future

object HelloServerHttp2 {

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

    val syncHandler: HttpRequest => HttpResponse = {
      case HttpRequest(GET, Uri.Path("/"), _, _, _) => HttpResponse(entity = "Hello world")

    }

    val asyncHandler: HttpRequest => Future[HttpResponse] = {
      case req => Future.successful(syncHandler(req))

    }

    implicit val actorSystem       = ActorSystem("system")
    implicit val actorMaterializer = ActorMaterializer()

    Http().bindAndHandleAsync(asyncHandler, interface = "localhost", port = 8443, ExampleServerContext)

    println("http2 server started at 8443")
  }
}
