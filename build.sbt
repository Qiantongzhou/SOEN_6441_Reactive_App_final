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

libraryDependencies += guice
libraryDependencies += "org.mockito" % "mockito-core" % "5.7.0" % Test

libraryDependencies ++= Seq(
  "org.junit.jupiter" % "junit-jupiter-api" % "5.9.2" % Test,  // JUnit 5 API
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.9.2" % Test // JUnit 5 Engine
)

libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.6.21" % Test