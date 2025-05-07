package sturdy.language.wasm.simple

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.*
import sturdy.effect.symboltable.DecidableSymbolTable
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.analyses.*
import sturdy.language.wasm.generic.*
import sturdy.values.integer.NumericInterval
import swam.{FuncType, ValType}

import java.nio.file.{Files, Path, Paths}

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
    "indirect-call-type-based" // bad test, we can easily determine the function that is called statically so no need for the signature filtering
  )

  private val benchmarksWithHost = List(
    "direct-call-imported-func",
    "host-callbacks-exports",
    "host-reachable-table-export",
    "host-reachable-table-import",
    "memory-init-offset-imported-global",
    "table-init-offset-imported-global"
  )

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

  private def makeHostModule(funs: HostFunction*): (ModuleInstance, GlobalInit) =
    val inst = new ModuleInstance(Some("host"))
    funs.zipWithIndex.foreach { case (fun, ix) =>
      inst.exports :+= fun.name -> ExternalValue.Function(ix)
      inst.functionTypes :+= fun.funcType
      inst.addFunction(FunctionInstance.Host(inst, ix, fun))
    }
    inst -> noGlobalInit

  type GlobalInit = DecidableSymbolTable[Unit, GlobalAddr, IntervalAnalysis.Value] => Unit
  val noGlobalInit: GlobalInit = _ => ()

  private val imports: Map[String, (ModuleInstance, GlobalInit)] = Map(
    "direct-call-imported-func" -> makeHostModule(new HostFunction("print", FuncType(Vector(ValType.I32), Vector()))),
    "host-callbacks-exports" -> makeHostModule(new HostFunction("imported", FuncType(Vector(), Vector()))),
    "host-reachable-table-export" -> makeHostModule(new HostFunction("imported", FuncType(Vector(), Vector()))),
    "host-reachable-table-import" -> makeHostModule(new HostFunction("imported", FuncType(Vector(), Vector()))),
    "memory-init-offset-imported-global" -> {
      val (mod, _) = makeHostModule()
      mod.exports :+= "data_offset" -> ExternalValue.Global(0)
      mod.globalAddrs :+= GlobalAddr(0)
      mod -> (globals => globals.set((), GlobalAddr(0), IntervalAnalysis.Value.Int32(NumericInterval.constant(1337))))
    },
    "table-init-offset-imported-global" -> {
      val (mod, _) = makeHostModule()
      mod.exports :+= "element_offset" -> ExternalValue.Global(0)
      mod.globalAddrs :+= GlobalAddr(0)
      mod -> (globals => globals.set((), GlobalAddr(0), IntervalAnalysis.Value.Int32(NumericInterval.constant(1))))
    }
  )

  benchmarks.foreach(b => dispatch("microbenchmarks/" ++ b, b, None))
  benchmarksWithHost.foreach( b => dispatch( "microbenchmarks/" ++ b, b, imports.get(b)))
  //realWorldPrograms.foreach( b => dispatch( "real-world-programs/" ++ b, b, None))

  def dispatch(folder: String, name: String, host: Option[(ModuleInstance, GlobalInit)] = None): Unit =
    val bin = Paths.get(base.resolve(folder ++ "/main.wasm"))
    val text = Paths.get(base.resolve(folder ++ "/main.wast"))

//    println(bin)

    if (Files.exists(bin))
      it must s"execute constant analysis with stacked states on benchmark $name" in {
        run(bin, true, StackConfig.StackedStates(), host)
      }
    else if (Files.exists(text))
      it must s"execute constant analysis with stacked states on benchmark $name" in {
        run(text, false, StackConfig.StackedStates(), host)
      }
    else
      throw new Exception("Test file not found")


  def run(p: Path, binary: Boolean, stackConfig: StackConfig, host: Option[(ModuleInstance, GlobalInit)]): Unit =
    Fixpoint.DEBUG = false
    val module = if (binary) Parsing.fromBinary(p) else Parsing.fromText(p)

    val interp = new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty,
      WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Innermost(stackConfig))))

    val cfg = IntervalAnalysis.controlFlow(CfgConfig.AllNodes(true), interp)

    val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)
//    interp.addControlObserver(new PrintingControlObserver()(println))


    val res = interp.failure.fallible {
      interp.initializeThis()
      host.foreach(_._2(interp.globals))
      val modInst = interp.initializeModule(module, host.map(m => "host" -> m._1).toMap)
      if (modInst.exportedFunctions.contains("main")) {
        interp.invokeExported(modInst, "main", List())
      } else {
        interp.runMostGeneralClient(modInst, IntervalAnalysis.typedTop)
      }
    }
    println(res)

    val graph = graphBuilder.get
    val reachableFuns = graph.nodes.collect {
      case Node.BlockStart(funcId: FuncId) => funcId
    }
    val callEdges = graph.edges.collect {
      case e@Edge(Node.BlockStart(InstLoc.InFunction(from, _)), Node.BlockStart(to: FuncId), _) => from -> to
      case e@Edge(Node.BlockStart(InstLoc.InInit(mod, _)), Node.BlockStart(to: FuncId), _) => mod -> to
    }

    println(s"Reachable functions (${reachableFuns.size}): " + reachableFuns.mkString(", "))
    println(s"Call edges (${callEdges.size}): ")
    callEdges.foreach(e => println(s"  $e"))

    //    println(graphBuilder.get.toGraphViz)
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".interval.dot")
    println(dotPath)
    Files.writeString(dotPath, graph.toGraphViz)

    //println(cfg.toGraphViz)
//    recorder.events.foreach(println)
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




