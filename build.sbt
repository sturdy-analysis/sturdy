name := "sturdy"

ThisBuild / organization := "de.uni-mainz.informatik.pl"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "3.0.1"

Test / parallelExecution := false

libraryDependencies += "org.typelevel" %% "cats-parse" % "0.3.4"

Test / fork := false
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"
