package sturdy.language.wasm.benchmarksgame

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.{ControlEventChecker, ControlEventGraphBuilder, ControlEventParser, PrintingControlObserver, RecordingControlObserver}
import sturdy.effect.failure.AFallible
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.*
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.{ConcreteInterpreter, Parsing}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*

class BenchmarksgameConstantControlEventsTest extends AnyFlatSpec, Matchers:
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

    val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty,
      WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Innermost(stackConfig))))
    interp.addControlObserver(new ControlEventChecker)
    val parser = interp.addControlObserver(new ControlEventParser)
    val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)

    val modInst = interp.initializeModule(module)

    val res = Profiler.addTime("analysis") {
      interp.failure.fallible(
        interp.invokeExported(modInst, funcName, List.empty)
      )
    }
    LinearStateOperationCounter.addToListAndReset()
    //    println(interp.effectStack.getAllState)
    //    println(s"${LinearStateOperationCounter.toString} in the last tests")
    //    println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")
    Profiler.printLastMeasured()

//    val tree = parser.getFinalTree
//    val treeSequence = tree.print
//    val tree2 = ControlEventParser.parse(treeSequence)
//    val treeSequence2 = tree2.print
//
//    assert(treeSequence == treeSequence2)
//    assert(tree == tree2)

    val graphFromEvents = graphBuilder.get
    println(s"Graph size: ${graphFromEvents.edges.size}")
    val tree = parser.getFinalTree
    println(s"Tree size: ${tree.size}")

    if (tree.size < 50000) {
      val graphFromTree = tree.toGraph
      val edgesMissing = graphFromTree.edges.diff(graphFromEvents.edges)
      val edgesUnexpected = graphFromEvents.edges.diff(graphFromTree.edges)
      assertResult(Set(), "Edges missing in graph from events")(edgesMissing)
      assertResult(Set(), "Edges superfluous in graph from events")(edgesUnexpected)
    }
//    println(graphFromEvents.toGraphViz)

