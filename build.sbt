
ThisBuild / organization := "de.uni-mainz.informatik.pl"
ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))
ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "3.1.2"

ThisBuild / Test / fork := false
ThisBuild / Test / parallelExecution := false
ThisBuild / Test / testForkedParallel := false
ThisBuild / Test / logBuffered := false

lazy val root = (project in file("."))
  .settings(name := "sturdy")
  .aggregate(
    sturdy_core,
    sturdy_tip,
    sturdy_wasm,
    sturdy_tutorial
  )
  .settings(skip / publish := true)

lazy val sturdy_core = (project in file("sturdy-core"))
  .settings(
    name := "sturdy_core",
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-math3" % "3.6.1",
      "org.eclipse.collections" % "eclipse-collections" % "11.0.0",
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )

lazy val sturdy_apron_domain = (project in file("sturdy-apron-domain"))
  .dependsOn(sturdy_core % "compile->compile")
  .settings(
    name := "sturdy_apron_domain",
    libraryDependencies ++= Seq(
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )

lazy val sturdy_tip = (project in file("sturdy-tip"))
  .dependsOn(sturdy_core % "compile->compile")
  .dependsOn(sturdy_apron_domain % "compile->compile")
  .settings(
    name := "sturdy_tip",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "0.3.4",
      "org.typelevel" %% "cats-core" % "2.6.1",
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )

lazy val sturdy_pcf = (project in file("sturdy-pcf"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .settings(
    name := "sturdy_pcf",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "0.3.4",
      "org.typelevel" %% "cats-core" % "2.6.1",
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
    name := "sturdy_wasm",
    libraryDependencies ++= Seq(
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test",
      "org.json4s" %% "json4s-native" % "4.0.4" % "test",
      ("org.typelevel" %% "cats-parse" % "0.3.4").cross(CrossVersion.for3Use2_13) % "test",
      "org.xerial" % "sqlite-jdbc" % "3.36.0.3" % "test"
    )
  )

lazy val sturdy_tutorial = (project in file("sturdy-tutorial"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .settings(
    name := "sturdy_tutorial",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "0.3.4",
      "org.typelevel" %% "cats-core" % "2.6.1",
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )
