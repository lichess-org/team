ThisBuild / scalaVersion := "3.8.2"
ThisBuild / organization := "org.lichess"

lazy val app = (project in file("."))
  .settings(
    name := "app",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "cask" % "0.11.0",
      "org.polyvariant" %% "sttp-oauth2" % "0.21.0",
      "org.scala-lang" %% "toolkit" % "0.7.0",
    ),
    fork := true
  )
