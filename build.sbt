ThisBuild / scalaVersion := "2.13.15"

ThisBuild / version := "1.0-SNAPSHOT"
// Akka dependencies
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.21", // Adjust version as needed
  "com.typesafe.akka" %% "akka-stream" % "2.6.21"
)



lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """SOEN_6441_Reactive_App_final""",
    libraryDependencies ++= Seq(
      guice
    )
  )