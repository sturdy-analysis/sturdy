package sturdy.language.wasm.benchmarksgame

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.{ControlEventGraphBuilder, ControlGraph, PrintingControlObserver, RecordingControlObserver}
import sturdy.effect.failure.AFallible
import sturdy.fix
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm
import sturdy.language.wasm.abstractions.Control.{Atom, Section}
import sturdy.language.wasm.{ConcreteInterpreter, Parsing, compareControlGraphs, newEdgesTotal, newNodesTotal, testCfgDifference}
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
import java.net.URI
import java.nio.file.attribute.FileAttribute
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.jdk.StreamConverters.*

class BenchmarksgameConstantTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame (recompiled) constant analysis"

  val funcName = "_start"
  val uri: URI = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.foreach { p =>
    it must s"execute constant analysis with stacked states on benchmark ${p.getFileName} (innermost_noInter)" in {
      val g1 = run(p, binary = true, StackConfig.StackedStates(storeIntermediateOutput = false), fix.iter.Config.Innermost).withName("No intermediate")
      println(s"${g1.name}:   ${g1.nodes.size} nodes, ${g1.edges.size} edges")
    }
    it must s"execute constant analysis with stacked states on benchmark ${p.getFileName} (innermost_withInter)" in {
      val g2 = run(p, binary = true, StackConfig.StackedStates(storeIntermediateOutput = true), fix.iter.Config.Innermost).withName("With intermediate")
      println(s"${g2.name}:   ${g2.nodes.size} nodes, ${g2.edges.size} edges")
    }
    it must s"execute constant analysis with stacked states on benchmark ${p.getFileName} (outermost_noInter)" in {
      val g1 = run(p, binary = true, StackConfig.StackedStates(storeIntermediateOutput = false), fix.iter.Config.Outermost).withName("No intermediate")
      println(s"${g1.name}:   ${g1.nodes.size} nodes, ${g1.edges.size} edges")
    }
    it must s"execute constant analysis with stacked states on benchmark ${p.getFileName} (outermost_withInter)" in {
      val g2 = run(p, binary = true, StackConfig.StackedStates(storeIntermediateOutput = true), fix.iter.Config.Outermost).withName("With intermediate")
      println(s"${g2.name}:   ${g2.nodes.size} nodes, ${g2.edges.size} edges")
    }
    it must s"execute constant analysis with stacked states on benchmark ${p.getFileName} (topmost_noInter)" in {
      val g1 = run(p, binary = true, StackConfig.StackedStates(storeIntermediateOutput = false), fix.iter.Config.Topmost).withName("No intermediate")
      println(s"${g1.name}:   ${g1.nodes.size} nodes, ${g1.edges.size} edges")
    }
    it must s"execute constant analysis with stacked states on benchmark ${p.getFileName} (topmost_withInter)" in {
      val g2 = run(p, binary = true, StackConfig.StackedStates(storeIntermediateOutput = true), fix.iter.Config.Topmost).withName("With intermediate")
      println(s"${g2.name}:   ${g2.nodes.size} nodes, ${g2.edges.size} edges")
    }
  }

  def run(p: Path, binary: Boolean, stackConfig: StackConfig, iterConfig: fix.iter.Config): ControlGraph[Atom, Section] =
    Fixpoint.DEBUG = false

    val name = p.getFileName
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    Profiler.reset()
    val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty,
      WasmConfig(fix = FixpointConfig(stack = stackConfig, iter = iterConfig)))
    val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)

    val modInst = interp.instantiateModule(module)

    val res = Profiler.addTime("analysis") {
      interp.failure.fallible(
        interp.invokeExported(modInst, funcName, List.empty)
      )
    }
    
    val newCfg = graphBuilder.get
    val dotPath2 = p.getParent.resolve(p.getFileName.toString + ".constant.dot")
    Files.writeString(dotPath2, newCfg.toGraphViz)

    Profiler.printLastMeasured()
    newCfg
