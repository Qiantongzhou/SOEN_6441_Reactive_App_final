ThisBuild / scalaVersion := "2.13.15"

ThisBuild / version := "1.0-SNAPSHOT"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.8.4",
  "com.typesafe.akka" %% "akka-stream" % "2.8.4",
  "com.typesafe.play" %% "play-akka-http-server" % "2.8.20"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """SOEN_6441_Reactive_App_final""",
    libraryDependencies ++= Seq(
      guice
    )
  )