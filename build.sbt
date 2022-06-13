val V = new {
  val Http4s = "0.23.12"
  val Circe = "0.14.2"
  val Doobie = "1.0.0-RC1"
  val Flyway = "8.5.12"
  val Logback = "1.2.10"
  val Munit = "0.7.29"
  val MunitCatsEffect = "1.0.7"
}

lazy val root = (project in file("."))
  .settings(
    organization := "ba.sake",
    name := "snowplow-tech-test",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % V.Http4s,
      "org.http4s" %% "http4s-ember-client" % V.Http4s,
      "org.http4s" %% "http4s-circe" % V.Http4s,
      "org.http4s" %% "http4s-dsl" % V.Http4s,
      "io.circe" %% "circe-generic" % V.Circe,
      "com.github.pureconfig" %% "pureconfig" % "0.17.1",
      // db
      "org.tpolecat" %% "doobie-core" % V.Doobie,
      "org.tpolecat" %% "doobie-hikari" % V.Doobie,
      "org.tpolecat" %% "doobie-postgres" % V.Doobie, // Postgres driver + type mappings.
      "org.flywaydb" % "flyway-core" % V.Flyway,

      // runtime
      "ch.qos.logback" % "logback-classic" % V.Logback % Runtime,

      // test
      "org.scalameta" %% "munit" % V.Munit % Test,
      "org.typelevel" %% "munit-cats-effect-3" % V.MunitCatsEffect % Test
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
