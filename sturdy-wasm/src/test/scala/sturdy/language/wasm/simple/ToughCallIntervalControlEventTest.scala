package sturdy.language.wasm.simple

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.*
import sturdy.effect.failure.AFallible
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, Control, ControlFlow}
import sturdy.language.wasm.analyses.*
import sturdy.language.wasm.generic.*
import sturdy.language.wasm.{ConcreteInterpreter, Parsing, testCfgDifference}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.Topped
import swam.binary.ModuleParser
import swam.syntax.{Func, Module}
import swam.validation.Validator
import swam.{FuncType, ModuleLoader, ValType}

import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.jdk.StreamConverters.*

class ToughCallIntervalControlEventTest extends AnyFlatSpec, Matchers:
  behavior of "That’s a Tough Call mini-benchmarks with interval analysis"

  private val base = this.getClass.getResource("/sturdy/language/wasm/thatsatoughcall/").toURI

  private val benchmarks = List(
    "cpp-vtable-layout-source-type-info", // doesn't work
    "direct-call-simple",
    "direct-call-transitive",
    "entry-point-start",
    "entry-point-wasi",
    "host-code-table-mutable", // unsound because the table is exported and can be modified by the host code
    "indirect-call-func-in-table",
    "indirect-call-index-expr-const",
    "indirect-call-index-expr-double-load",
    "indirect-call-index-expr-interprocedural-param",
    "indirect-call-index-expr-interprocedural-result",
    "indirect-call-index-expr-load-const-addr",
    "indirect-call-index-expr-load-interprocedural-param",
    "indirect-call-index-expr-load-interprocedural-result",
    "indirect-call-index-expr-local",
    "indirect-call-index-expr-mask", // weird test, deterministic if interprocedural analysis
    "indirect-call-index-expr-memory-mutable", // Fixpoint MostGeneralClient issue
    "indirect-call-type-based") // bad test, we can easily determine the function that is called statically so no need for the signature filtering

  private val benchmarksWithHost = List(
    "direct-call-imported-func",
    "host-callbacks-exports",
    "host-reachable-table-export",
    "host-reachable-table-import",
    "memory-init-offset-imported-global",
    "table-init-offset-imported-global")

  /* private val realWorldPrograms = List(
    "blake3",
    "fonteditor-core",
    "hpcc-lib-graphviz",
    "magic",
    "opencv-wasm",
    "opusscript",
    "shiki",
    "source-map",
    "sql.js",
    "wasm-rsa"
  ) */

  private val imports: Map[String, Imports] = Map.empty

  benchmarks.foreach(b => dispatch("microbenchmarks/" ++ b, b, None))
  //benchmarksWithHost.foreach( b => dispatch( "microbenchmarks/" ++ b, b, imports.get(b)))
  //realWorldPrograms.foreach( b => dispatch( "real-world-programs/" ++ b, b, None))

  def dispatch(folder: String, name: String, imports: Option[Imports] = None): Unit =
    val bin = Paths.get(base.resolve(folder ++ "/main.wasm"))
    val text = Paths.get(base.resolve(folder ++ "/main.wast"))

    println(bin)

    if (Files.exists(bin))
      it must s"execute constant analysis with stacked states on benchmark $name" in {
        run(bin, true, StackConfig.StackedStates())
      }
    else if (Files.exists(text))
      it must s"execute constant analysis with stacked states on benchmark $name" in {
        run(text, false, StackConfig.StackedStates())
      }
    else
      throw new Exception("Test file not found")


  def run(p: Path, binary: Boolean, stackConfig: StackConfig): Unit =
    Fixpoint.DEBUG = false
    val module = if (binary) Parsing.fromBinary(p) else Parsing.fromText(p)


    val interp = new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty,
      WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Innermost(stackConfig))))

    val cfg = IntervalAnalysis.controlFlow(CfgConfig.AllNodes(true), interp)

    val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)
    val recorder = interp.addControlObserver(new RecordingControlObserver)

    val modInst = interp.initializeModule(module)

    val res = interp.failure.fallible(
      if module.exports.nonEmpty then interp.runMostGeneralClient(modInst, IntervalAnalysis.typedTop)
    )

    println(graphBuilder.get.toGraphViz)
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".interval.dot")
    println(dotPath)
    Files.writeString(dotPath, graphBuilder.get.toGraphViz)

    //println(cfg.toGraphViz)
    recorder.events.foreach(println)
//testCfgDifference(cfg, graphBuilder.get)

class FilterObserver[Atom, Section, Exc, Fx](f: ControlEvent[Atom, Section, Exc, Fx] => Boolean) extends
  ControlObserver[Atom, Section, Exc, Fx],
  ControlObservable[Atom, Section, Exc, Fx]:

  override def handle(ev: BasicControlEvent[Atom, Section, Exc, Fx]): Unit = if f(ev) then super.triggerControlEvent(ev)

  override def handle(ev: ExceptionControlEvent[Atom, Section, Exc, Fx]): Unit = if f(ev) then super.triggerControlEvent(ev)

  override def handle(ev: BranchingControlEvent[Atom, Section, Exc, Fx]): Unit = if f(ev) then super.triggerControlEvent(ev)

  override def handle(ev: FixpointControlEvent[Atom, Section, Exc, Fx]): Unit = if f(ev) then super.triggerControlEvent(ev)

  override def joinStart(): Unit = triggerControlEvent(BranchingControlEvent.Fork())

  override def joinSwitch(leftFailed: Boolean): Unit = triggerControlEvent(BranchingControlEvent.Switch())

  override def joinEnd(leftFailed: Boolean, rightFailed: Boolean): Unit = triggerControlEvent(BranchingControlEvent.Join())

  override def repeating(): Unit = triggerControlEvent(FixpointControlEvent.Restart())

  override def throwing(exc: Exc): Unit = triggerControlEvent(ExceptionControlEvent.Throw(exc))

  override def handlingStart(exc: Exc): Unit = triggerControlEvent(ExceptionControlEvent.BeginHandle(exc))

  override def handlingEnd(): Unit = triggerControlEvent(ExceptionControlEvent.EndHandle())

  override def tryStart(): Unit = triggerControlEvent(ExceptionControlEvent.BeginTry())

  override def tryEnd(): Unit = triggerControlEvent(ExceptionControlEvent.EndTry())




