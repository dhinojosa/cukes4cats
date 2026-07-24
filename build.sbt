ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "proposed-cuke",
    version := "0.0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.7.0",
      "org.typelevel" %% "cats-parse" % "1.1.0",
      "org.scalameta" %% "munit" % "1.3.4" % Test,
    )
  )
