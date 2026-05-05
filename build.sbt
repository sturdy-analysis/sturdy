
ThisBuild / organization := "de.uni-mainz.informatik.pl"
ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))
ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "3.3.5"

ThisBuild / Test / fork := false
ThisBuild / Test / parallelExecution := false
ThisBuild / Test / testForkedParallel := false
ThisBuild / Test / logBuffered := false

ThisBuild / assemblyMergeStrategy := {
  // Discard empty/directory entries that cause ZipFileSystem issues
  case "" => MergeStrategy.discard
  case PathList("META-INF", xs @ _*) =>
    xs match {
      case "MANIFEST.MF" :: Nil => MergeStrategy.discard
      case "services" :: _      => MergeStrategy.concat
      case _                    => MergeStrategy.discard
    }
  case "module-info.class"    => MergeStrategy.discard
  case PathList("META-INF", "versions", xs @ _*) => MergeStrategy.first
  case PathList("shapeless", xs @ _*) if xs.exists(_.contains("?")) =>
    MergeStrategy.discard
  case PathList("shapeless", xs @ _*) if xs.exists(x =>
    x.contains("\u003f") || x.matches(".*\\$qmark.*")) => MergeStrategy.discard
  case x => MergeStrategy.last
}

lazy val root = (project in file("."))
  .settings(name := "sturdy")
  .aggregate(
    sturdy_core,
    sturdy_apron,
    sturdy_apron_bench,
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
      "org.scalatest" %% "scalatest" % "3.2.9" % "test",
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.17.0" % "test"
    ),
    assembly / assemblyJarName := "sturdy-core.jar"
  )

val copyApronBinaries = taskKey[Unit]("Copies the pqlatform-dependent Apron binaries to the project root, so that sbt loads them automatically")

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
    assembly / assemblyJarName := "sturdy-apron.jar"
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
    ),
    assembly / assemblyJarName := "sturdy-tip.jar"
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
    ),
    assembly / assemblyJarName := "sturdy-pcf.jar"
  )

val swam = file("sturdy-wasm/swam")

lazy val sturdy_wasm = (project in file("sturdy-wasm"))
  .dependsOn(sturdy_core % "compile->compile;test->test")
  .dependsOn(sturdy_apron % "compile->compile")
  .dependsOn(ProjectRef(swam, "swam_core") % "compile->compile;test->test")
  .dependsOn(ProjectRef(swam, "swam_text") % "compile->compile;test->test")
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
    ),
    dependencyOverrides ++= Seq(
      "org.typelevel" %% "cats-core"   % "2.12.0",
      "org.typelevel" %% "cats-kernel" % "2.12.0",
      "org.typelevel" %% "cats-free"   % "2.12.0"
    ),
    // Include test dependencies in main assembly
    assembly / fullClasspath ++= (Test / fullClasspath).value,
    assembly / assemblyJarName := "sturdy-wasm.jar"
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
