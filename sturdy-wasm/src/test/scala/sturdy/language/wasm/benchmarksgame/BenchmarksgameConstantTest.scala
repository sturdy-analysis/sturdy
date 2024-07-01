package sturdy.language.wasm.benchmarksgame

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.{ControlEventGraphBuilder, PrintingControlObserver, RecordingControlObserver}
import sturdy.effect.failure.AFallible
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm
import sturdy.language.wasm.{ConcreteInterpreter, Parsing, testCfgDifference}
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.*
import sturdy.language.wasm.generic.FrameData
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.io.{BufferedOutputStream, FileOutputStream}
import java.nio.file.attribute.FileAttribute
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.jdk.StreamConverters.*

class BenchmarksgameConstantTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame (recompiled) constant analysis"

  val funcName = "_start"
  val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.headOption.foreach { p =>
    it must s"warm-up constant analysis on benchmark ${p.getFileName}" in {
      run(p, binary = true, StackConfig.StackedStates())
      LinearStateOperationCounter.clearAll()
      Profiler.reset()
    }
  }

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.foreach { p =>
    it must s"execute constant analysis with stacked states on benchmark ${p.getFileName}" in {
      run(p, binary = true, StackConfig.StackedStates())
    }
  }

  def run(p: Path, binary: Boolean, stackConfig: StackConfig) =
    Fixpoint.DEBUG = false
    
    val name = p.getFileName
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val logpath = p.getParent.resolve(p.getFileName.toString + ".constant.new.log")
    Files.deleteIfExists(logpath)
    val f = Files.createFile(logpath)
    val buf = BufferedOutputStream(new FileOutputStream(f.toFile))

    val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty,
      WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Innermost(stackConfig))))
    val oldCfg = ConstantAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    interp.addControlObserver(new PrintingControlObserver()({ s =>
      buf.write(s.getBytes)
      buf.write("\n".getBytes)
    }))
    val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)
    
    val modInst = interp.initializeModule(module)

    val res = Profiler.addTime("analysis") {
      interp.failure.fallible(
        interp.invokeExported(modInst, funcName, List.empty)
      )
    }
    
    val newCfg = graphBuilder.get

    val dotPath = p.getParent.resolve(p.getFileName.toString + ".types.dot")
    Files.writeString(dotPath, oldCfg.toGraphViz)

    val dotPath2 = p.getParent.resolve(p.getFileName.toString + ".types.new.dot")
    Files.writeString(dotPath2, newCfg.toGraphViz)

    testCfgDifference(oldCfg, newCfg)

