
ThisBuild / organization := "de.uni-mainz.informatik.pl"
ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))
ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "3.1.0"

ThisBuild / Test / fork := false
ThisBuild / Test / parallelExecution := false
ThisBuild / Test / testForkedParallel := false
ThisBuild / Test / logBuffered := false

lazy val root = (project in file("."))
  .settings(name := "sturdy")
  .aggregate(
    sturdy_core,
    sturdy_scheme,
    sturdy_tip,
    sturdy_wasm
  )
  .settings(skip / publish := true)

lazy val sturdy_core = (project in file("sturdy-core"))
  .settings(
    name := "sturdy-core",
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-math3" % "3.6.1",
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )

lazy val sturdy_jimple = (project in file("sturdy-jimple"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .settings(
    name := "sturdy-jimple",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "0.3.4",
      "org.typelevel" %% "cats-core" % "2.6.1",
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
      "org.typelevel" %% "cats-core" % "2.6.1",
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

val swamCommit = "39999a1751076c6dbfe2a92c874f17683730d14e"
val swam = uri(s"https://gitlab.rlp.net/plmz/external/swam.git#$swamCommit")

lazy val sturdy_wasm = (project in file("sturdy-wasm"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .dependsOn(ProjectRef(swam, "swam_core") % "compile->compile;test->test")
  .dependsOn(ProjectRef(swam, "swam_text") % "test->test")
  .settings(
    name := "sturdy-wasm",
    libraryDependencies ++= Seq(
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    )
  )

//lazy val sturdy_wasm_benchmarks = (project in file("sturdy-wasm-benchmarks"))
//  .dependsOn(sturdy_wasm % "compile->compile")
//  .dependsOn(ProjectRef(swam, "swam_core") % "compile->compile")
//  .dependsOn(ProjectRef(swam, "swam_text") % "compile->compile")
//  .settings(
//    name := "sturdy-wasm-benchmarks"
//  )
//  .enablePlugins(JmhPlugin)