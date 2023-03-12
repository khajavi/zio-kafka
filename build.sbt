lazy val scala212  = "2.12.17"
lazy val scala213  = "2.13.10"
lazy val scala3    = "3.2.1"
lazy val mainScala = scala213
lazy val allScala  = Seq(scala212, scala3, mainScala)

lazy val kafkaVersion         = "3.2.0"
lazy val embeddedKafkaVersion = "3.3.1" // Should be the same as kafkaVersion, except for the patch part

lazy val kafkaClients          = "org.apache.kafka"           % "kafka-clients"           % kafkaVersion
lazy val zio                   = "dev.zio"                   %% "zio"                     % zioVersion.value
lazy val zioStreams            = "dev.zio"                   %% "zio-streams"             % zioVersion.value
lazy val zioTest               = "dev.zio"                   %% "zio-test"                % zioVersion.value
lazy val zioTestSbt            = "dev.zio"                   %% "zio-test-sbt"            % zioVersion.value
lazy val scalaCollectionCompat = "org.scala-lang.modules"    %% "scala-collection-compat" % "2.9.0"
lazy val jacksonDatabind       = "com.fasterxml.jackson.core" % "jackson-databind"        % "2.14.1"
lazy val logback               = "ch.qos.logback"             % "logback-classic"         % "1.3.5"
lazy val embeddedKafka         = "io.github.embeddedkafka"   %% "embedded-kafka"          % embeddedKafkaVersion

enablePlugins(ZioSbtCiPlugin, ScalafixPlugin)

inThisBuild(
  List(
    name                     := "ZIO Kafka",
    zioVersion               := "2.0.5",
    ciEnabledBranches        := Seq("master"),
    useCoursier              := false,
    scalaVersion             := mainScala,
    crossScalaVersions       := allScala,
    Test / parallelExecution := false,
    Test / fork              := true,
    run / fork               := true,
    developers := List(
      Developer(
        "iravid",
        "Itamar Ravid",
        "iravid@iravid.com",
        url("https://github.com/iravid")
      )
    )
  )
)

val excludeInferAny = { options: Seq[String] => options.filterNot(Set("-Xlint:infer-any")) }

lazy val root = project
  .in(file("."))
  .settings(
    name           := "zio-kafka",
    publish / skip := true
  )
  .aggregate(
    zioKafka,
    zioKafkaTestUtils,
    zioKafkaTest,
    docs
  )

def stdSettings_ = Seq(
  scalafmtOnCompile := true,
  Compile / compile / scalacOptions ++= optionsOn("2.13")("-Wconf:cat=unused-nowarn:s").value,
  scalacOptions -= "-Xlint:infer-any",
  // workaround for bad constant pool issue
  (Compile / doc) := Def.taskDyn {
    val default = (Compile / doc).taskValue
    Def.task(default.value)
  }.value
)

lazy val zioKafka =
  project
    .in(file("zio-kafka"))
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-kafka", packageName = Some("zio.kafka")))
    .settings(
      libraryDependencies ++= Seq(
        zioStreams,
        kafkaClients,
        jacksonDatabind,
        scalaCollectionCompat
      )
    )

lazy val zioKafkaTestUtils =
  project
    .in(file("zio-kafka-test-utils"))
    .dependsOn(zioKafka)
    .settings(stdSettings("zio-kafka-test-utils", packageName = Some("zio.kafka")))
    .settings(
      libraryDependencies ++= Seq(
        zio,
        kafkaClients,
        scalaCollectionCompat
      ) ++
        dependenciesOnOrElse("3")(
          embeddedKafka
            .cross(CrossVersion.for3Use2_13) exclude ("org.scala-lang.modules", "scala-collection-compat_2.13")
        )(embeddedKafka)
    )

lazy val zioKafkaTest =
  project
    .in(file("zio-kafka-test"))
    .dependsOn(zioKafka, zioKafkaTestUtils)
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-kafka-test", packageName = Some("zio.kafka")))
    .settings(publish / skip := true)
    .settings(
      libraryDependencies ++= Seq(
        zioStreams,
        zioTest    % Test,
        zioTestSbt % Test,
        kafkaClients,
        jacksonDatabind,
        logback % Test,
        scalaCollectionCompat
      ) ++
        dependenciesOnOrElse("3")(
          embeddedKafka
            .cross(CrossVersion.for3Use2_13) exclude ("org.scala-lang.modules", "scala-collection-compat_2.13")
        )(embeddedKafka),
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
    )

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val docs = project
  .in(file("zio-kafka-docs"))
  .settings(
    moduleName := "zio-kafka-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    projectName                                := "ZIO Kafka",
    mainModuleName                             := (zioKafka / moduleName).value,
    projectStage                               := ProjectStage.ProductionReady,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(zioKafka),
    readmeCredits :=
      "This library is heavily inspired and made possible by the research and implementation done in " +
        "[Alpakka Kafka](https://github.com/akka/alpakka-kafka), a library maintained by the Akka team and originally " +
        "written as Reactive Kafka by SoftwareMill.",
    readmeLicense +=
      "\n\n" + """|Copyright 2021 Itamar Ravid and the zio-kafka contributors. All rights reserved.
                  |<!-- TODO: not all rights reserved, rather Apache 2... -->""".stripMargin
  )
  .enablePlugins(WebsitePlugin)
