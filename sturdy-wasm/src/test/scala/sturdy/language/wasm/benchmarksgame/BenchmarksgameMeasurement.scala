package sturdy.language.wasm.benchmarksgame

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.{ControlEventGraphBuilder, ControlGraph, PrintingControlObserver, RecordingControlObserver}
import sturdy.effect.failure.{AFallible, CollectedFailures}
import sturdy.fix
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm
import sturdy.language.wasm.*
import sturdy.language.wasm.abstractions.Control.{Atom, Section}
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.*
import sturdy.language.wasm.generic.FrameData
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.io.{BufferedOutputStream, File, FileOutputStream, IOException}
import java.net.URI
import java.nio.file.attribute.FileAttribute
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.jdk.StreamConverters.*
import java.io.PrintWriter

object BenchmarksgameMeasurement:
  val outFile: File = new File("wasm_benchmark.csv")
  val warmups: Int = 5
  val runs: Int = 10

  val funcName = "_start"
  val uri: URI = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  @main
  def main(): Unit =
    val files = Files.list(Paths.get(uri)).toScala(List).filter(p => !p.toString.contains("_O") && p.toString.endsWith(".wasm")).sorted

    val stackConfigs = Seq(
      StackConfig.StackedStates(storeIntermediateOutput = false, storeNonrecursiveOutput = false),
      StackConfig.StackedStates(storeIntermediateOutput = true, storeNonrecursiveOutput = true)
    )

    val strategies = Seq(
      fix.iter.Config.Innermost,
      fix.iter.Config.Outermost,
      fix.iter.Config.Topmost
    )

    val instances: Map[String, WasmConfig => Interpreter#GenericInstance] = Map(
      "Interval" -> ((wasmConfig: WasmConfig) => new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty, wasmConfig)),
      "Constant" -> ((wasmConfig: WasmConfig) => new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty, wasmConfig)),
      "Type" -> ((wasmConfig: WasmConfig) => new TypeAnalysis.Instance(FrameData.empty, Iterable.empty, wasmConfig))
    )

    val header = Seq("prog", "analysis", "strategy", "caching", "timeInMs")

    val data = files.flatMap { p =>
      instances.flatMap { (analysisName, instantiateInterp) =>
        strategies.flatMap { strategy =>
          stackConfigs.flatMap { stackConfig =>
            val isCaching = stackConfig match
              case StackedStates(_, true, true, _) => true
              case StackedStates(_, false, false, _) => false
              case _ => throw IllegalStateException("Unsupported configuration setup")

            println(s"Run: ${p.getFileName} :: ${analysisName} :: ${strategy} :: ${isCaching}")
            val execTimes = run(p, binary = true, stackConfig, strategy, analysisName, instantiateInterp)
            // build rows
            execTimes.map { t =>
              Seq(p.getFileName.toString, analysisName, strategy.toString, isCaching.toString, t.toString)
            }
          }
        }
      }
    }

    // Write CSV file
    val writer = new PrintWriter(outFile)
    (header +: data).foreach { row =>
      writer.println(row.mkString(","))
    }
    writer.close()

  def collectGarbage(): Unit = {
    System.gc()
    try {
      Thread.sleep(2000)
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }

  def run(p: Path, binary: Boolean, stackConfig: StackConfig, iterConfig: fix.iter.Config, analysisName: String, instantiateInterp: WasmConfig =>  Interpreter#GenericInstance ): Seq[Long] =
    Fixpoint.DEBUG = false

    val config = WasmConfig(fix = FixpointConfig(stack = stackConfig, iter = iterConfig))
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    collectGarbage()

    println("\tStart warmup...")
    for (i <- 0.until(warmups)) {
      val interp = instantiateInterp(config)
      val modInst = interp.initializeModule(module)

      interp.failure.asInstanceOf[CollectedFailures[?]].fallible(
        interp.invokeExported(modInst, funcName, List.empty)
      )
    }
    println("\tFinished warmup!")

    collectGarbage()

    println("\tMeasure...")
    val res = for (i <- 0.until(runs)) yield {
      val interp = instantiateInterp(config)
      val modInst = interp.initializeModule(module)

      val optStart = System.currentTimeMillis()
      interp.failure.asInstanceOf[CollectedFailures[?]].fallible(
        interp.invokeExported(modInst, funcName, List.empty)
      )
      val optTime = System.currentTimeMillis() - optStart

      collectGarbage()

      optTime
    }
    println("\tDone!")
    res

