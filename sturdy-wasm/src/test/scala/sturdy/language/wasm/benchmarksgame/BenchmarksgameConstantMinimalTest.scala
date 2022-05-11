package sturdy.language.wasm.benchmarksgame

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.AFallible
import sturdy.fix.Fixpoint
import sturdy.language.wasm
import sturdy.language.wasm.{ConcreteInterpreter, Parsing}
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.*
import sturdy.language.wasm.generic.FrameData
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*

class BenchmarksgameConstantMinimalTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame (recompiled) constant analysis"

  val funcName = "_start"
  val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.headOption.foreach { p =>
    it must s"warm-up constant analysis on benchmark ${p.getFileName}" in {
      run(p, binary = true)
      LinearStateOperationCounter.clearAll()
      Profiler.reset()
    }
  }

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.foreach { p =>
    it must s"execute constant analysis on benchmark ${p.getFileName}" in {
      run(p, binary = true)
    }
  }

  def run(p: Path, binary: Boolean = false) =
    Fixpoint.DEBUG = false
    
    val name = p.getFileName
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val interp = new ConstantAnalysisSturdyInstance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Outermost)))

    val modInst = interp.initializeModule(module)

    val res = Profiler.addTime("analysis") {
      interp.failure.fallible(
        interp.invokeExported(modInst, funcName, List.empty)
      )
    }
    LinearStateOperationCounter.addToListAndReset()
    println(interp.analysisState.getAllState)
    println(s"${LinearStateOperationCounter.toString} in the last tests")
    println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")
    Profiler.printLastMeasured()
