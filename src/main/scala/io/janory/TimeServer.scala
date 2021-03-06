/*
 * Copyright 2015 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.janory

import akka.NotUsed
import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.PermanentRedirect
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.sse.ServerSentEvent

import scala.concurrent.duration.DurationInt

object TimeServer {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val mat    = ActorMaterializer()
    Http().bindAndHandle(route, "localhost", 8000)
  }

  private def route = {
    import Directives._
    import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._

    def assets =
      getFromResourceDirectory("web") ~ pathSingleSlash(
        get(redirect("index.html", PermanentRedirect))
      )

    def events =
      path("events") {
        get {
          complete {
           val so: ToResponseMarshallable =  Source
              .tick(2.seconds, 2.seconds, NotUsed)
              .map(_ => LocalTime.now())
              .map(timeToServerSentEvent)
              .keepAlive(1.second, () => ServerSentEvent.heartbeat)
            so
          }
        }
      }

    assets ~ events
  }

  private def timeToServerSentEvent(time: LocalTime) =
    ServerSentEvent(DateTimeFormatter.ISO_LOCAL_TIME.format(time))
}