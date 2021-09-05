ThisBuild / organization := "de.uni-mainz.informatik.pl"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "3.0.1"
ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

Test / parallelExecution := false
Test / fork := false

lazy val root = (project in file("."))
  .settings(name := "sturdy")
  .aggregate(
    sturdy_core,
    sturdy_tip
  )
  .settings(skip / publish := true)

lazy val sturdy_core = (project in file("sturdy-core"))
  .settings(
    name := "sturdy-core",
    cd ..++= Seq(
      "org.apache.commons" % "commons-math3" % "3.6.1",
      // TODO: Add Apron Library done
      //"apron" % "apron" % "0.9.13" at "lib/apron.jar",
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )

lazy val sturdy_tip = (project in file("sturdy-tip"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .settings(
    name := "sturdy-tip",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "0.3.4",
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )

lazy val sturdy_scheme = (project in file("sturdy-scheme"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .settings(
    name := "sturdy-scheme",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "0.3.4",
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )


lazy val sturdy_minijava = (project in file("sturdy-minijava"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .settings(
    name := "sturdy-minijava",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "0.3.4",
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )

lazy val sturdy_wasm = (project in file("sturdy-wasm"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .settings(
    name := "sturdy-wasm",
    libraryDependencies ++= Seq(
      ("org.gnieh" % "swam-core" % "0.6.0-RC4").cross(CrossVersion.for3Use2_13),
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test",
      ("org.gnieh" % "swam-text" % "0.6.0-RC4").cross(CrossVersion.for3Use2_13) % "test"
    )
  )
