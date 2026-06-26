inThisBuild(
  Seq(
    scalaVersion := "3.8.4",
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
    name := "team",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "cask" % "0.11.0",
      "com.lihaoyi" %% "scalatags" % "0.13.1",
      "com.softwaremill.sttp.client4" %% "circe" % "4.0.25",
      "org.scala-lang" %% "toolkit" % "0.9.2",
      "org.scala-lang" %% "toolkit-test" % "0.9.2" % Test,
      "com.outr" %% "scribe" % "3.19.0",
    ),
    fork := true,
    Compile / mainClass  := Some("App"),
    dockerBaseImage      := "eclipse-temurin:25-jdk-noble",
    dockerRepository     := Some("ghcr.io"),
    Docker / packageName := "lichess-org/team",
    dockerAliases        := {
      val repo = dockerRepository.value
      val name = (Docker / packageName).value
      sys.env.getOrElse("DOCKER_LABELS", "latest").split(",").toSeq.map(tag =>
        DockerAlias(repo, None, name, Some(tag.trim))
      )
    },
    dockerExposedPorts   := Seq(8080),
    dockerEnvVars := Map(
      "VERSION" -> sys.env.getOrElse("VERSION", "dev")
    )
  )
