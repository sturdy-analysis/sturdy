package sturdy.language.wasm.precision

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

object SlowTest extends org.scalatest.Tag("SlowTest")

class RelationalAnalysisPrecisionTests extends Suites(
  new RelationalAnalysisTest(Polka(true), relational = true),
  new RelationalAnalysisTest(Octagon(), relational = true),
  new RelationalAnalysisTest(Box(), relational = true),
  new RelationalAnalysisTest(Polka(true), relational = false),
)

class RelationalAnalysisTest(manager: Manager, relational: Boolean = true) extends AnyFlatSpec, Matchers:
  behavior of (manager.getClass.getSimpleName)

  val precisionTestPath = Paths.get(this.getClass.getResource("/sturdy/language/wasm/precision.wast").toURI);

  val precisionTest = RoundingMode.withRoundingMode(RoundingDir.Nearest) {Parsing.testscript(precisionTestPath)}

  val analysis = new RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(), ctx = Insensitive, relational = relational))

  type AValue = RelationalAnalysis.Value
  val modules: mutable.Map[String, ModuleInstance] = mutable.Map()
  var currentModule: ModuleInstance = null
  var imports: Map[String, ModuleInstance] = Map()

  val convertVals: unresolved.Expr => List[RelationalAnalysis.Value] = constExprToAVals

  type AResult = AFallible[List[AValue]]

  def run(filename: Path, commands: Seq[Command]): Unit =
    commands.foreach { command =>
      eval(command)
      analysis.garbageCollect()
    }

  def getAModule(module: Option[String]): ModuleInstance = module match
    case None => currentModule
    case Some(name) => modules(name)

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
      imports += s -> getAModule(id)
    case BinaryModule(id, bytes) =>
      val mod = Parsing.fromBytes(bytes)
      loadModule(id, mod)
    case QuotedModule(id, text) => {}
    case AssertReturn(action, expectedRes) =>
      val aRes = runAAction(action, convertVals)
      ???
    case AssertReturnCanonicalNaN(action) => // skip
    case AssertReturnArithmeticNaN(action) => // skip
    case AssertTrap(action: Action, message: String) => // skip
    case AssertModuleTrap(mod,_) => // skip
    case _: AssertUnlinkable => // skip
    case _: AssertInvalid => // skip
    case _: AssertMalformed => // skip
    case _: AssertExhaustion => // skip
    case _: Meta => // skip

  def loadModule(id: Option[String], mod: Module): Unit =
    RoundingMode.withRoundingMode(RoundingDir.Nearest) {
      val aModInst = analysis.instantiateModule(mod, imports)
      id match
        case None => // nothing
        case Some(name) => modules += name -> aModInst
      currentModule = aModInst
    }

  def runAAction(a: Action, convertVals: unresolved.Expr => List[RelationalAnalysis.Value]): AResult = a match {
    case Invoke(modName, fun, expr) => evalAInvoke(modName, fun, convertVals(expr))
    case Get(modName, name) => evalAGet(modName, name)
  }

  def evalAInvoke(module: Option[String], fun: String, vals: List[AValue]): AResult =
    val modInst = getAModule(module)
    analysis.failure.fallible {
      analysis.invokeExported(modInst, fun, vals)
    }

  def evalAGet(module: Option[String], name: String): AResult =
    val modInst = getAModule(module)
    val exp = modInst.exports.find(_._1 == name)
    assert(exp.isDefined, s"export $name not found in ${module.getOrElse("current")}")
    exp.get._2 match
      case Global(addr) =>
        val globalIdx = modInst.globalAddrs.lift(addr).getOrElse(throw new Error(s"Unbound global $addr"))
        val value = analysis.getGlobalValue(globalIdx)
        AFallible.Unfailing(List(value))
      case ext =>
        throw new IllegalArgumentException(s"Can only get globals, but $name was $ext")

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

class NumTestCases:
  var n: Int = 0
  def increment(): Unit = n += 1
  override def toString: String = n.toString