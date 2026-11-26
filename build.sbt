ThisBuild / tlBaseVersion := "0.1" // your current series x.y

ThisBuild / organization := "com.evolutionnext"
ThisBuild / organizationName := "Evolutionnext"
ThisBuild / startYear := Some(2026)
ThisBuild / licenses := Seq(License.MIT)
ThisBuild / developers ++= List(
  tlGitHubDev("dhinojosa", "Daniel Hinojosa")
)

val Scala212 = "2.12.21"
val Scala213 = "2.13.18"
val Scala3 = "3.3.0"
val Java17 = JavaSpec.temurin("17")
val Java21 = JavaSpec.temurin("21")
val Java25 = JavaSpec.temurin("25")

ThisBuild / crossScalaVersions := Seq(Scala212, Scala213, Scala3)
ThisBuild / scalaVersion := Scala3 // the default Scala
ThisBuild / tlJdkRelease := Some(17)
ThisBuild / tlFatalWarnings := true
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / githubWorkflowJavaVersions :=
  Seq(Java17, Java21, Java25)
ThisBuild / githubWorkflowBuildMatrixExclusions := Seq(
  MatrixExclude(Map("project" -> "rootJS", "java" -> Java21.render)),
  MatrixExclude(Map("project" -> "rootJS", "java" -> Java25.render))
)

ThisBuild / githubWorkflowGeneratedCI ~= (_.map {
  case job if job.id == "dependency-submission" =>
    job.withPermissions(
      Some(
        org
          .typelevel
          .sbt
          .gha
          .Permissions
          .Specify
          .defaultRestrictive
          .withContents(org.typelevel.sbt.gha.PermissionValue.Write)
      )
    )
  case job => job
})

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
      "org.scalameta" %% "munit" % "1.3.6" % Test,
      "org.typelevel" %% "munit-cats-effect" % "2.2.1" % Test,
      "org.scalameta" %% "munit-scalacheck" % "1.3.0" % Test
    )
  )

addCommandAlias(
  "verify",
  ";clean;+test;++ 2.13;scalafixAll --check;headerCheckAll;scalafmtCheckAll;scalafmtSbtCheck"
)

addCommandAlias(
  "format",
  ";clean;++ 2.13;scalafixAll;scalafmtAll;scalafmtSbt"
)
