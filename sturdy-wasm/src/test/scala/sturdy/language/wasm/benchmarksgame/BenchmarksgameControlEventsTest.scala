package sturdy.language.wasm.benchmarksgame

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.{BasicControlEvent, BranchingControlEvent, ControlEventChecker, ControlEventGraphBuilder, ControlEventParser, ControlObservable, ControlObserver, ExceptionControlEvent, FixpointControlEvent, PrintingControlObserver, RecordingControlObserver}
import sturdy.effect.failure.{AFallible, CollectedFailures}
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, Control, ControlFlow}
import sturdy.language.wasm.analyses.*
import sturdy.language.wasm.generic.{FrameData, WasmFailure}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter, Parsing}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*

class BenchmarksgameConstantControlEventsTest extends BenchmarksgameControlEventsTest[ConstantAnalysis.type](ConstantAnalysis, ConstantAnalysis.Instance(_,_,_))

class BenchmarksgameControlEventsTest[Interp <: Interpreter](val interp: Interp, newInstance: (FrameData,Iterable[interp.Value],WasmConfig) => interp.Instance) extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame (recompiled)"

  val funcName = "_start"
  val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.headOption.foreach { p =>
//    it must s"warm-up constant analysis on benchmark ${p.getFileName}" in {
//      run(p, binary = true, StackConfig.StackedStates())
//      LinearStateOperationCounter.clearAll()
//      Profiler.reset()
//    }
  }

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.foreach { p =>
    it must s"benchmark ${p.getFileName}" in {
      run(p, binary = true, StackConfig.StackedStates())
      Profiler.reset()
    }
  }

  def run(p: Path, binary: Boolean, stackConfig: StackConfig) =
    Fixpoint.DEBUG = false

    val name = p.getFileName
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val inst = newInstance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Topmost(stackConfig))))
    inst.addControlObserver(new ControlEventChecker)
    var count = 0
    import sturdy.language.wasm.abstractions.Control.*
    inst.addControlObserver(new ControlObserver {
      override def handle(ev: BasicControlEvent[Atom, Section, Exc, Fx]): Unit = count += 1
      override def handle(ev: ExceptionControlEvent[Atom, Section, Exc, Fx]): Unit = count += 1
      override def handle(ev: BranchingControlEvent[Atom, Section, Exc, Fx]): Unit = count += 1
      override def handle(ev: FixpointControlEvent[Atom, Section, Exc, Fx]): Unit = count += 1
    })
    val parser = inst.addControlObserver(new ControlEventParser)
    val graphBuilder = inst.addControlObserver(new ControlEventGraphBuilder)
//    val printingControlObserver = inst.addControlObserver(new PrintingControlObserver("  ", "\n")(println))

    val modInst = inst.initializeModule(module)

    println(s"Running analysis on ${p.getFileName}")
    val res = Profiler.addTime("analysis") {
      inst.failure.asInstanceOf[CollectedFailures[WasmFailure]].fallible(
        inst.invokeExported(modInst, funcName, List.empty)
      )
    }
//    LinearStateOperationCounter.addToListAndReset()
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

    println(s"Events count: $count")
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

