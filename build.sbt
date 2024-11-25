ThisBuild / scalaVersion := "2.13.15"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """SOEN_6441_Reactive_App_final""",
    libraryDependencies ++= Seq(
      guice
    )
  )