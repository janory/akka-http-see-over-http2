import sbt._

object Dependencies {
  // Versions
  lazy val akkaVersion            = "2.5.8"
  lazy val akkaHttpVersion        = "10.0.11"
  lazy val akkaHttp2Version       = "10.1.0-RC1"
  lazy val scalaTestVersion       = "3.0.4"
  lazy val logbackVersion         = "1.2.3"
  lazy val scalaPbVersion         = "0.6.7"
  lazy val java8CompatVersion     = "0.8.0"
  lazy val ficusVersion           = "1.4.3"
  lazy val circeVersion           = "0.9.1"
  lazy val akkaHttpCirceVersion   = "1.20.0-RC1"
  lazy val mockitoVersion         = "2.13.0"

  // Libraries
  val AkkaActor                 = "com.typesafe.akka"      %% "akka-actor"                   % akkaVersion
  val AkkaStream                = "com.typesafe.akka"      %% "akka-stream"                  % akkaVersion
  val AkkaSlf4j                 = "com.typesafe.akka"      %% "akka-slf4j"                   % akkaVersion
  val AkkaTestkit               = "com.typesafe.akka"      %% "akka-testkit"                 % akkaVersion
  val AkkaStreamTestkit         = "com.typesafe.akka"      %% "akka-stream-testkit"          % akkaVersion
  val AkkaHttp                  = "com.typesafe.akka"      %% "akka-http"                    % akkaHttpVersion
  val AkkaHttp2                 = "com.typesafe.akka"      %% "akka-http2-support"           % akkaHttp2Version
  val AkkaHttpTestkit           = "com.typesafe.akka"      %% "akka-http-testkit"            % akkaHttpVersion
  val ScalaTest                 = "org.scalatest"          %% "scalatest"                    % scalaTestVersion
  val LogbackClassic            = "ch.qos.logback"         % "logback-classic"               % logbackVersion
  val Java8Compat               = "org.scala-lang.modules" %% "scala-java8-compat"           % java8CompatVersion
  val Ficus                     = "com.iheart"             %% "ficus"                        % ficusVersion
  val CirceCore                 = "io.circe"               %% "circe-core"                   % circeVersion
  val CirceGeneric              = "io.circe"               %% "circe-generic"                % circeVersion
  val CirceParser               = "io.circe"               %% "circe-parser"                 % circeVersion
  val CirceOptics               = "io.circe"               %% "circe-optics"                 % circeVersion
  val CirceJava8                = "io.circe"               %% "circe-java8"                  % circeVersion
  val AkkaHttpCirce             = "de.heikoseeberger"      %% "akka-http-circe"              % akkaHttpCirceVersion
  val MockitoCore               = "org.mockito"            % "mockito-core"                  % mockitoVersion

  val settings = Seq(
    AkkaActor,
    AkkaStream,
//    AkkaHttp,
    AkkaHttp2,
    AkkaSlf4j,
    AkkaHttpCirce,
    Java8Compat,
    Ficus,
    LogbackClassic,
    CirceCore,
    CirceGeneric,
    CirceParser,
    CirceOptics,
    CirceJava8,
    AkkaTestkit              % "test",
    AkkaStreamTestkit        % "test",
    AkkaHttpTestkit          % "test",
    ScalaTest                % "test",
    MockitoCore              % "test"
  )
}