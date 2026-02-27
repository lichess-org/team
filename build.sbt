ThisBuild / scalaVersion := "3.8.2"
ThisBuild / organization := "org.lichess"

lazy val invites = (project in file("."))
  .settings(
    name := "invites",
    libraryDependencies ++= Seq(
        "org.scala-lang" %% "toolkit" % "0.7.0"
    )
  )
