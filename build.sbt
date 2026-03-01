ThisBuild / scalaVersion := "3.8.2"
ThisBuild / organization := "org.lichess"

lazy val app = (project in file("."))
  .settings(
    name := "app",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "cask" % "0.11.0"
    ),
    fork := true
  )
