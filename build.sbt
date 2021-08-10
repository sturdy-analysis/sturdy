
ThisBuild / organization := "de.uni-mainz.informatik.pl"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "3.0.1"
ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

Test / parallelExecution := false
Test / fork := false

lazy val root = (project in file("."))
  .settings(name := "sturdy")
  .aggregate(
    sturdy_core
  )
  .settings(skip / publish := true)

lazy val sturdy_core = (project in file("sturdy-core"))
  .settings(
    name := "sturdy-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "0.3.4",
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )
