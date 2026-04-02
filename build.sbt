import NativePackagerHelper._

inThisBuild(
  Seq(
    scalaVersion := "3.8.2",
    organization := "org.lichess",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= Seq(
      "-Wunused:all"
    )
  )
)

lazy val app = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    name := "lichess-invites",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "cask" % "0.11.0",
      "com.lihaoyi" %% "scalatags" % "0.13.1",
      "com.softwaremill.sttp.client4" %% "circe" % "4.0.19",
      "org.scala-lang" %% "toolkit" % "0.9.1",
      "org.scala-lang" %% "toolkit-test" % "0.9.1" % Test,
    ),
    fork := true,
    Compile / mainClass  := Some("App"),
    dockerBaseImage      := "eclipse-temurin:25-jdk-noble",
    dockerRepository     := Some("ghcr.io"),
    Docker / packageName := "fitztrev/lichess-invites",
    dockerUpdateLatest   := true,
    dockerExposedPorts   := Seq(8080),
    dockerEnvVars := Map(
      "VERSION_FILE" -> "/opt/docker/extra/version.txt"
    ),
    Universal / mappings ++= directory("extra")
  )
