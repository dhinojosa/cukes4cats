ThisBuild / tlBaseVersion := "0.1" // your current series x.y

ThisBuild / organization := "com.evolutionnext"
ThisBuild / organizationName := "Evolutionnext"
ThisBuild / startYear := Some(2026)
ThisBuild / licenses := Seq(License.MIT)
ThisBuild / developers ++= List(
    // your GitHub handle and name
    tlGitHubDev("dhinojosa", "Daniel Hinojosa")
)

val Scala3 = "3.3.0"
ThisBuild / crossScalaVersions := Seq("2.13.11", Scala3)
ThisBuild / scalaVersion := Scala3 // the default Scala

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JVMPlatform, JSPlatform)
    .in(file("core"))
    .settings(
        name := "cukes4cats-core",
        description := "Core Cucumber for Typelevel Cats",
        libraryDependencies ++= Seq(
            "org.typelevel" %% "cats-core" % "2.13.0",
            "org.typelevel" %% "cats-effect" % "3.7.0",
            "org.typelevel" %% "cats-parse" % "1.1.0",
            "org.scalameta" %% "munit" % "1.3.4" % Test,
            "org.typelevel" %% "munit-cats-effect" % "2.2.0" % Test
        )
    )


