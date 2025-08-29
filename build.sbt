
ThisBuild / organization := "de.uni-mainz.informatik.pl"
ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))
ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "3.3.5"

ThisBuild / Test / fork := false
ThisBuild / Test / parallelExecution := false
ThisBuild / Test / testForkedParallel := false
ThisBuild / Test / logBuffered := false

lazy val root = (project in file("."))
  .settings(name := "sturdy")
  .aggregate(
    sturdy_core,
    sturdy_apron,
    sturdy_apron_bench,
    sturdy_tip,
    sturdy_wasm,
    sturdy_tutorial,
    sturdy_jvm_bytecode
  )
  .settings(skip / publish := true)

lazy val sturdy_core = (project in file("sturdy-core"))
  .settings(
    name := "sturdy_core",
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-math3" % "3.6.1",
      "org.eclipse.collections" % "eclipse-collections" % "11.0.0",
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test",
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.17.0" % "test"
    )
  )

val copyApronBinaries = taskKey[Unit]("Copies the platform-dependent Apron binaries to the project root, so that sbt loads them automatically")

lazy val sturdy_apron: Project = (project in file("sturdy-apron"))
  .dependsOn(sturdy_core % "compile->compile; test->test")
  .settings(
    name := "sturdy_apron",
    libraryDependencies ++= Seq(
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    ),
    copyApronBinaries := {
      println("Copies Apron binaries")
      val (os, ext) = System.getProperty("os.name").toLowerCase match {
        case s if s.contains("darwin") || s.contains("mac") => ("darwin", "dylib")
        case s if s.contains("win") => ("win", "dll")
        case s if s.contains("nix") || s.contains("linux") => ("unix", "so")
      }
      val arch = System.getProperty("os.arch")
      val nativeDir = baseDirectory.value / "lib_extra" / s"$os-$arch"
      if (!nativeDir.exists) {
        println(s"No Apron binaries for $os on $arch available in ${baseDirectory.value / "lib_extra"}.")
        println(s"Please create $nativeDir and add Apron binaries there.")
        throw new FeedbackProvidedException {}
      }
      val files = Seq() ++ nativeDir.listFiles().filter(_.name.endsWith(ext))
      for (source <- files) {
        val target = baseDirectory.value / source.name
        println(s"Copies $source to $target")
        java.nio.file.Files.copy(source.file.toPath, target.file.toPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
      }
    },
//    Compile / compile  := ((Compile / compile) dependsOn copyApronBinaries).value
  )

lazy val sturdy_apron_bench: Project = (project in file("sturdy-apron-bench"))
  .dependsOn(sturdy_apron % "compile->compile; test->test")
  .enablePlugins(JmhPlugin)
  .settings(
    javaOptions += "-Djava.library.path=/home/sven/sturdy.scala/sturdy-apron/result/lib/",
    Jmh / sourceDirectory := (Test / sourceDirectory).value,
    Jmh / classDirectory := (Test / classDirectory).value,
    Jmh / dependencyClasspath := (Test / dependencyClasspath).value,
    // rewire tasks, so that 'bench/Jmh/run' automatically invokes 'bench/Jmh/compile' (otherwise a clean 'bench/Jmh/run' would fail)
    Jmh / compile := (Jmh / compile).dependsOn(Test / compile).value,
    Jmh / run := (Jmh / run).dependsOn(Jmh / compile).evaluated
  )

lazy val sturdy_tip = (project in file("sturdy-tip"))
  .dependsOn(sturdy_core % "compile->compile")
  .dependsOn(sturdy_apron % "compile->compile")
  .settings(
    name := "sturdy_tip",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "1.1.0",
      "org.typelevel" %% "cats-core" % "2.13.0",
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )

lazy val sturdy_pcf = (project in file("sturdy-pcf"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .settings(
    name := "sturdy_pcf",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "1.1.0",
      "org.typelevel" %% "cats-core" % "2.13.0",
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )

val swamCommit = "580bdb83208e63cbdb2bcec86fc5432db288fd21"
val swam = uri(s"https://gitlab.rlp.net/plmz/external/swam.git#$swamCommit")

lazy val sturdy_wasm = (project in file("sturdy-wasm"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .dependsOn(sturdy_apron % "compile->compile")
  .dependsOn(ProjectRef(swam, "swam_core") % "compile->compile;test->test")
  .dependsOn(ProjectRef(swam, "swam_text") % "test->test")
  .settings(
    name := "sturdy_wasm",
    libraryDependencies ++= Seq(
      // test
      "org.scalatest" %% "scalatest" % "3.2.9" % "test",
      "org.json4s" %% "json4s-native" % "4.0.4" % "test",
      "com.github.tototoshi" %% "scala-csv" % "2.0.0" % "test",
      ("org.typelevel" %% "cats-parse" % "1.1.0").cross(CrossVersion.for3Use2_13) % "test",
      "org.xerial" % "sqlite-jdbc" % "3.36.0.3" % "test",
      ("io.circe" %% "circe-yaml" % "0.16.0").cross(CrossVersion.for3Use2_13) % "test"
    )
  )

lazy val sturdy_tutorial = (project in file("sturdy-tutorial"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .settings(
    name := "sturdy_tutorial",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "1.1.0",
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    )
  )

val opalCommit = "48991def3e1b61d376af7c77c94d24124b417187"
val opal = uri(s"https://github.com/opalj/opal.git#$opalCommit")

lazy val sturdy_jvm_bytecode = (project in file("sturdy-jvm-bytecode"))
  .dependsOn(sturdy_core % "compile->compile")
  // https://github.com/opalj/opal/pull/143 introduced support for class file version 65 (java 21)
  // however, using commit e4bd7f58a9c0698285e325e3c80d339af951dc8d causes issues, so we just use the latest commit on the default branch as of writing
  .dependsOn(ProjectRef(opal, "BytecodeRepresentation") % "compile->compile")
  .settings(
    name := "sturdy_jvm_bytecode",
    libraryDependencies ++= Seq(
      // there has been no release that supports class file version 65 as of 7.7.25, so we depend on the commit that introduced support for it.
      // "de.opal-project" % "framework_2.13" % "5.0.0"
      // testing
      "org.scalatest" %% "scalatest" % "3.2.19" % "test"
    ),
    // resolve conflicts between scala-xml_2.13 and scala-xml_3 by excluding one of them
    excludeDependencies ++= Seq(
      ExclusionRule("org.scala-lang.modules", "scala-xml_2.13")
    ),
    scalacOptions += "-deprecation",
  )

//lazy val sturdy_wasm_benchmarks = (project in file("sturdy-wasm-benchmarks"))
//  .dependsOn(sturdy_wasm % "compile->compile")
//  .dependsOn(ProjectRef(swam, "swam_core") % "compile->compile")
//  .dependsOn(ProjectRef(swam, "swam_text") % "compile->compile")
//  .settings(
//    name := "sturdy-wasm-benchmarks"
//  )
//  .enablePlugins(JmhPlugin)
