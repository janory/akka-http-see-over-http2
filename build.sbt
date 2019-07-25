lazy val commonSettings = Seq(
  organization := "io.janory",
  scalaVersion := "2.12.4",
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding",
    "UTF-8"
  ),
  javacOptions ++= Seq(
    "-source",
    "1.8",
    "-target",
    "1.8"
  ),
  fork in Test := true
)

lazy val `akka-http-sse-over-http2` = project
  .in(file("."))
  .enablePlugins(JavaAgent)
  .settings(
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.6"
  )
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.settings
  )



