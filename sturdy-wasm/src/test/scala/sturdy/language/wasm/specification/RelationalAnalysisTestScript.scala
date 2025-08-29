package sturdy.language.wasm.testscript

import apron.*
import cats.effect.{Blocker, IO}

import scala.collection.mutable
import scala.io.Source
import scala.jdk.StreamConverters.*
import org.scalatest.Assertions.*
import org.scalatest.{BeforeAndAfterAll, Suites}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.apron.{RoundingDir, RoundingMode}
import sturdy.control.{ControlEventChecker, ControlEventGraphBuilder, PrintingControlObserver}
import sturdy.effect.failure.{AFallible, CFallible, given}
import sturdy.{*, given}
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm.{ConcreteInterpreter, Parsing}
import ConcreteInterpreter.{constExprToVal, constExprToVals, eqVals}
import sturdy.language.wasm.analyses.{RelationalAnalysis, *}
import sturdy.language.wasm.generic.ExternalValue.Global
import sturdy.language.wasm.generic.{ExternalValue, FrameData, ModuleInstance, WasmFailure}
import sturdy.util.Profiler
import sturdy.values.{*, given}
import sturdy.values.integer.given
import swam.syntax.Module
import swam.text.*
import swam.text.unresolved.{FreshId, NoId, SomeId}

import java.io.File
import java.nio.file.{Files, Path, Paths}
import com.github.tototoshi.csv.*

val csvWriter = {
  val writer = CSVWriter.open(File("relational-test-script.csv"))
  writer.writeRow(List("filename", "abstract_domain", "passed_cases", "test_cases", "percent_passed"))
  writer
}

object SlowTest extends org.scalatest.Tag("SlowTest")

class RelationalAnalysisSoundnessTests extends Suites(
  new RelationalAnalysisTestScript(Polka(true)),
  new RelationalAnalysisTestScript(Octagon()),
  new RelationalAnalysisTestScript(Box()),
), BeforeAndAfterAll:

  override def afterAll(): Unit = csvWriter.close()

class RelationalAnalysisTestScript(manager: Manager) extends AnyFlatSpec, Matchers:
  behavior of ("TestScript relational analysis with " + manager.getClass.getSimpleName)

  val pathSpectest = Paths.get(this.getClass.getResource("/sturdy/language/wasm/spectest.wast").toURI)
  val uri = this.getClass.getResource("/sturdy/language/wasm/spec-test-suite-wasm1").toURI;

  val spectest = RoundingMode.withRoundingMode(RoundingDir.Nearest) {Parsing.fromText(pathSpectest)}

  def analyses: IterableOnce[() => RelationalAnalysis.Instance] =
    val stackedStates = StackConfig.StackedStates(readPriorOutput = false, storeNonrecursiveOutput = false, observers = Seq())
    Iterator(
//      () => new RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Topmost(stackedStates)), ctx = Insensitive)),
      () => new RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(), ctx = Insensitive)),
//      () => new RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Outermost(stackedStates)), ctx = Insensitive)),
//      () => new RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Innermost(StackConfig.StackedCfgNodes())), ctx = Insensitive)),
//      () => new RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Innermost(false)), ctx = Insensitive)),
//      () => new RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Outermost(true)), ctx = Insensitive)),
//      () => new RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Outermost(false)), ctx = Insensitive)),
//      () => new RelationalAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Innermost), ctx = CallSites(1))),
//      () => new RelationalAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Topmost), ctx = CallSites(1))),
    )

  def isSlow(manager: Manager, script: String): Boolean =
    script.contains("float_exprs.wast")

  def runTest(scriptPath: Path, analysis: RelationalAnalysis.Instance): Unit =
    val script = RoundingMode.withRoundingMode(RoundingDir.Nearest) {Parsing.testscript(scriptPath)}
    val interp = RelationalAnalysisTestScriptInterpreter(Some(spectest), analysis)
    interp.run(scriptPath.getFileName, script)

  Fixpoint.DEBUG = false
  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    p.toString.endsWith("left-to-right.wast")
  ).sorted.foreach { p =>
    for (analysis <- analyses) {
      val anl = analysis()
      if (isSlow(anl.apronManager, p.getFileName.toString))
        it must s"execute ${p.getFileName} with ${anl}" taggedAs (SlowTest) in {
          runTest(p, anl)
          Profiler.printLastMeasured()
          Profiler.reset()
        }
      else
        it must s"execute ${p.getFileName} with ${anl}" in {
          runTest(p, anl)
          Profiler.printLastMeasured()
          Profiler.reset()
        }

    }
  }

class RelationalAnalysisTestScriptInterpreter(spectest: Option[Module] = None, aInterp: RelationalAnalysis.Instance, useTop: Boolean = false):
  import aInterp.given
  import sturdy.language.wasm.analyses.RelationalAnalysisSoundness.given

  type CValue = ConcreteInterpreter.Value
  type AValue = RelationalAnalysis.Value

  val cInterp = new ConcreteInterpreter.Instance(FrameData.empty, Iterable.empty)
  aInterp.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
  val cfg = aInterp.addControlObserver(new ControlEventGraphBuilder)
  val cModules: mutable.Map[String, ModuleInstance] = mutable.Map()
  val aModules: mutable.Map[String, ModuleInstance] = mutable.Map()
  var cCurrent: ModuleInstance = null
  var aCurrent: ModuleInstance = null
  var cImports: Map[String, ModuleInstance] = Map()
  var aImports: Map[String, ModuleInstance] = Map()
  val convertVals: unresolved.Expr => List[RelationalAnalysis.Value] =
    if (useTop)
      constExprToTops
    else
      constExprToAVals
  val passedTestCases = new NumTestCases
  val totalTestCases = new NumTestCases

  spectest.foreach{ mod =>
    val modInst = cInterp.instantiateModule(mod)
    cCurrent = modInst
    cImports += "spectest" -> modInst

    val amodInst = aInterp.instantiateModule(mod)
    aCurrent = amodInst
    aImports += "spectest" -> amodInst
  }


  type CResult = CFallible[List[CValue]]
  type AResult = AFallible[List[AValue]]

  def run(filename: Path, commands: Seq[Command]): Unit =
    try {
      commands.foreach { command =>
        eval(command)
        aInterp.garbageCollect()
      }
    } finally {
      val percentPassed = if(totalTestCases.n == 0) 100.0d else passedTestCases.n.toDouble / totalTestCases.n.toDouble * 100.0d
      csvWriter.writeRow(List(filename.toString, aInterp.apronManager.getClass.getSimpleName, passedTestCases.toString, totalTestCases.toString, f"$percentPassed%.1f"))
    }

  def getCModule(module: Option[String]): ModuleInstance = module match
    case None => cCurrent
    case Some(name) => cModules(name)

  def getAModule(module: Option[String]): ModuleInstance = module match
    case None => aCurrent
    case Some(name) => aModules(name)

  def eval(c: Command): Unit =
    c match
    case ValidModule(m) =>
      // validate and compile module
      val mod = Parsing.fromUnresolved(m)
      val id = m.id match
        case SomeId(name) => Some(name)
        case _ => None
      loadModule(id, mod)
    case Register(s, id) =>
      cImports += s -> getCModule(id)
      aImports += s -> getAModule(id)
    case BinaryModule(id, bytes) =>
      val mod = Parsing.fromBytes(bytes)
      loadModule(id, mod)
    case QuotedModule(id, text) =>
      ???
    case AssertReturn(action, expectedRes) =>
      totalTestCases.increment()
      val aRes = runAAction(action, convertVals)
      val res = runCAction(action)
      assert(!res.isFailing)
      val expected = constExprToVals(expectedRes)
      assert(eqVals(expected, res.get), c.toString + s", expected $expected, but got ${res.get}")
      assertResult(IsSound.Sound, s"result $aRes on assertion $c (top = $useTop)")(Soundness.isSound(res, aRes))
      assertResult(IsSound.Sound, s"interpreter states after running action $action (top = $useTop)")(Soundness.isSound(cInterp, aInterp))
      passedTestCases.increment()
    case AssertReturnCanonicalNaN(action) =>
      totalTestCases.increment()
      val aRes = runAAction(action, convertVals)
      val res = runCAction(action)
      checkNaN(res, c.toString)
      assertResult(IsSound.Sound, s"result $aRes on assertion $c (top = $useTop)")(Soundness.isSound(res, aRes))
      assertResult(IsSound.Sound, s"interpreter states after running action $action (top = $useTop)")(Soundness.isSound(cInterp, aInterp))
      passedTestCases.increment()
    case AssertReturnArithmeticNaN(action) =>
      totalTestCases.increment()
      val aRes = runAAction(action, convertVals)
      val res = runCAction(action)
      checkNaN(res, c.toString)
      assertResult(IsSound.Sound, s"result $aRes on assertion $c (top = $useTop)")(Soundness.isSound(res, aRes))
      assertResult(IsSound.Sound, s"interpreter states after running action $action (top = $useTop)")(Soundness.isSound(cInterp, aInterp))
      passedTestCases.increment()
    case AssertTrap(action: Action, message: String) =>
      totalTestCases.increment()
      val aRes = runAAction(action, convertVals)
      val res = runCAction(action)
      assert(res.isFailing, c.toString)
      assertResult(IsSound.Sound, s"result $aRes on assertion $c (top = $useTop)")(Soundness.isSound(res, aRes))
      assertResult(IsSound.Sound, s"interpreter states after running action $action (top = $useTop)")(Soundness.isSound(cInterp, aInterp))
      passedTestCases.increment()
    case AssertModuleTrap(mod,_) =>
      totalTestCases.increment()
      val aRes = aInstantiate(mod)
      val res = instantiate(mod)
      assert(res.isFailing, c.toString)
      //assertResult(IsSound.Sound, s"result after instantiating module $mod")(Soundness.isSound(res, aRes))
      assertResult(IsSound.Sound, s"interpreter states after instantiating module $mod")(Soundness.isSound(cInterp, aInterp))
      passedTestCases.increment()
    case _: AssertUnlinkable => // skip
    case _: AssertInvalid => // skip
    case _: AssertMalformed => // skip
    case _: AssertExhaustion => // skip
    case action: Action =>
      runAAction(action, convertVals)
      runCAction(action)
    case _: Meta => // skip

  def loadModule(id: Option[String], mod: Module): Unit =
    RoundingMode.withRoundingMode(RoundingDir.Nearest) {
      val cModInst = cInterp.instantiateModule(mod, cImports)
      id match
        case None => // nothing
        case Some(name) => cModules += name -> cModInst
      cCurrent = cModInst
    }
    val aModInst = aInterp.instantiateModule(mod, aImports)
    id match
      case None => // nothing
      case Some(name) => aModules += name -> aModInst
    aCurrent = aModInst
    // check for soundness of the interpreter states after initialization
    assertResult(IsSound.Sound, s"after initializing module $mod")(Soundness.isSound(cInterp, aInterp))

  def instantiate(t: TestModule): CFallible[ModuleInstance] =
    t match
      case ValidModule(m) =>
        val mod = Parsing.fromUnresolved(m)
        RoundingMode.withRoundingMode(RoundingDir.Nearest) {
          cInterp.failure.fallible {
            cInterp.instantiateModule(mod, cImports)
          }
        }
      case BinaryModule(id,s) => throw new Error("instantiation of binary modules not yet implemented.")
      case QuotedModule(id, s) => throw new Error("instantiation of quoted modules not yet implemented.")

  def aInstantiate(t: TestModule): AFallible[ModuleInstance] =
    t match
      case ValidModule(m) =>
        val mod = Parsing.fromUnresolved(m)
        aInterp.failure.fallible {
          aInterp.instantiateModule(mod, aImports)
        }
      case BinaryModule(id,s) => throw new Error("instantiation of binary modules not yet implemented.")
      case QuotedModule(id, s) => throw new Error("instantiation of quoted modules not yet implemented.")

  def runCAction(a: Action): CResult = RoundingMode.withRoundingMode(RoundingDir.Nearest) {
    a match {
      case Invoke(modName, fun, expr) => evalCInvoke(modName, fun, constExprToVals(expr))
      case Get(modName, name) => evalCGet(modName, name)
    }
  }

  def runAAction(a: Action, convertVals: unresolved.Expr => List[RelationalAnalysis.Value]): AResult = a match {
    case Invoke(modName, fun, expr) => evalAInvoke(modName, fun, convertVals(expr))
    case Get(modName, name) => evalAGet(modName, name)
  }

  def evalCInvoke(module: Option[String], fun: String, vals: List[CValue]): CResult =
    val modInst = getCModule(module)
    cInterp.failure.fallible {
      cInterp.invokeExported(modInst, fun, vals)
    }

  def evalAInvoke(module: Option[String], fun: String, vals: List[AValue]): AResult =
    val modInst = getAModule(module)
    aInterp.failure.fallible {
      aInterp.invokeExported(modInst, fun, vals)
    }

  def evalCGet(module: Option[String], name: String): CResult =
    val modInst = getCModule(module)
    val exp = modInst.exports.find(_._1 == name)
    assert(exp.isDefined, s"export $name not found in ${module.getOrElse("current")}")
    exp.get._2 match
      case Global(addr) =>
        val globalIdx = modInst.globalAddrs.lift(addr).getOrElse(throw new Error(s"Unbound global $addr"))
        val value = cInterp.getGlobalValue(globalIdx)
        CFallible.Unfailing(List(value))
      case ext =>
        throw new IllegalArgumentException(s"Can only get globals, but $name was $ext")

  def evalAGet(module: Option[String], name: String): AResult =
    val modInst = getAModule(module)
    val exp = modInst.exports.find(_._1 == name)
    assert(exp.isDefined, s"export $name not found in ${module.getOrElse("current")}")
    exp.get._2 match
      case Global(addr) =>
        val globalIdx = modInst.globalAddrs.lift(addr).getOrElse(throw new Error(s"Unbound global $addr"))
        val value = aInterp.getGlobalValue(globalIdx)
        AFallible.Unfailing(List(value))
      case ext =>
        throw new IllegalArgumentException(s"Can only get globals, but $name was $ext")

  def checkNaN(res: CResult, clue: String) =
    assert(!res.isFailing)
    val resClean: List[CValue] = res.get
    assert(resClean.size == 1, clue)
    val h = resClean.head
    assert(isNaN(h), clue)

  def constExprToAVals(e: unresolved.Expr): List[RelationalAnalysis.Value] =
    e.map(constExprToAVal).toList

  def constExprToAVal(inst: unresolved.Inst): RelationalAnalysis.Value = Abstractly(constExprToVal(inst))

  def constExprToTops(e: unresolved.Expr): List[RelationalAnalysis.Value] =
    e.map(constExprToTop).toList

  def constExprToTop(inst: unresolved.Inst): RelationalAnalysis.Value =
    inst match
      case unresolved.i32.Const(_) => RelationalAnalysis.Value.Num(RelationalAnalysis.NumValue.Int32(RelationalAnalysis.topI32))
      case unresolved.i64.Const(_) => RelationalAnalysis.Value.Num(RelationalAnalysis.NumValue.Int64(RelationalAnalysis.topI64))
      case unresolved.f32.Const(_) => RelationalAnalysis.Value.Num(RelationalAnalysis.NumValue.Float32(RelationalAnalysis.topF32))
      case unresolved.f64.Const(_) => RelationalAnalysis.Value.Num(RelationalAnalysis.NumValue.Float64(RelationalAnalysis.topF64))
      case _ => throw IllegalArgumentException(s"Expected constant instruction but got $inst")

  def isNaN(value: ConcreteInterpreter.Value): Boolean =
    value match
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f)) => f.isNaN
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d)) => d.isNaN
      case _ => false

class NumTestCases:
  var n: Int = 0
  def increment(): Unit = n += 1
  override def toString: String = n.toString