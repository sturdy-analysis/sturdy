package sturdy.language.wasm.specification

import cats.effect.Blocker
import cats.effect.IO
import org.scalatest.Assertions.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.ControlEventChecker
import sturdy.effect.failure.CFallible
import sturdy.effect.failure.{AFallible, given}
import sturdy.fix.Fixpoint
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.analyses.TypeAnalysis
import sturdy.language.wasm.analyses.TypeAnalysisSoundness.given
import sturdy.language.wasm.generic.ExternalValue.Global
import sturdy.language.wasm.generic.ExternalValue
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.generic.ModuleInstance
import sturdy.language.wasm.generic.WasmFailure
import sturdy.values.Topped
import sturdy.values.ordering.EqOps
import sturdy.values.Abstractly
import sturdy.values.PartialOrder
import sturdy.{IsSound, Soundness}
import sturdy.{*, given}
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.analyses.WasmConfig
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.text.*
import swam.text.unresolved.FreshId
import swam.text.unresolved.NoId
import swam.text.unresolved.SomeId
import swam.validation.Validator

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.collection.mutable
import scala.io.Source
import scala.jdk.StreamConverters.*




class TypeAnalysisTestSpec extends AnyFlatSpec, Matchers:
  behavior of "TestScript type analysis"

  val pathSpectest = Paths.get(this.getClass.getResource("/sturdy/language/wasm/spectest.wast").toURI)
  val uri = this.getClass.getResource("/sturdy/language/wasm/spec-test-suite-wasm1").toURI;

  val spectest = Parsing.fromText(pathSpectest)

  Fixpoint.DEBUG = false
  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wast")).sorted.foreach { p =>
    it must s"execute ${p.getFileName}" in {
      println(s"Executing TestScript type analysis on ${p.getFileName}")
      val script = Parsing.testscript(p)
      val interp = TypeAnalysisTestSpecInterpreter(Some(spectest))
      interp.run(script)
      val interpTop = TypeAnalysisTestSpecInterpreter(Some(spectest), true)
      interpTop.run(script)
    }
  }

class TypeAnalysisTestSpecInterpreter(spectest: Option[Module] = None, useTop: Boolean = false):
  type CValue = ConcreteInterpreter.Value
  type AValue = TypeAnalysis.Value

  val cInterp = new ConcreteInterpreter.Instance(FrameData.empty, Iterable.empty)
  val aInterp = new TypeAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig.default)
  aInterp.addControlObserver(new ControlEventChecker)
  val cModules: mutable.Map[String, ModuleInstance] = mutable.Map()
  val aModules: mutable.Map[String, ModuleInstance] = mutable.Map()
  var cCurrent: ModuleInstance = null
  var aCurrent: ModuleInstance = null
  var cImports: Map[String, ModuleInstance] = Map()
  var aImports: Map[String, ModuleInstance] = Map()
  val convertVals: unresolved.Expr => List[TypeAnalysis.Value] =
    if (useTop)
      constExprToTops
    else 
      constExprToAVals

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

  def eqVals(vs1: List[CValue], vs2: List[CValue]): Boolean =
    vs1.size == vs2.size && vs1.zip(vs2).forall {
      case (ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int32(i1)), ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int32(i2))) => i1 == i2
      case (ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int64(l1)), ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int64(l2))) => l1 == l2
      case (ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f1)), ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f2))) => f1.isNaN && f2.isNaN || f1 == f2
      case (ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d1)), ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d2))) => d1.isNaN && d2.isNaN || d1 == d2
      case _ => false
    }

  def run(commands: Seq[Command]): Unit =
    commands.map(c => {println(c); eval(c)})

  def getCModule(module: Option[String]): ModuleInstance = module match
    case None => cCurrent
    case Some(name) => cModules(name)

  def getAModule(module: Option[String]): ModuleInstance = module match
    case None => aCurrent
    case Some(name) => aModules(name)

  def eval(c: Command): Unit = c match
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
        val aRes = runAAction(action, convertVals)
        val res = runCAction(action)
        assert(!res.isFailing)
        val expected = constExprToVals(expectedRes)
        assert(eqVals(expected, res.get), c.toString + s" but $expected != ${res.get}")
        assertResult(IsSound.Sound, s"result after running action $action")(Soundness.isSound(res, aRes))
        assertResult(IsSound.Sound, s"interpreter states after running action $action")(Soundness.isSound(cInterp, aInterp))
      case AssertReturnCanonicalNaN(action) =>
        val aRes = runAAction(action, convertVals)
        val res = runCAction(action)
        checkNaN(res, c.toString)
        assertResult(IsSound.Sound, s"result after running action $action")(Soundness.isSound(res, aRes))
        assertResult(IsSound.Sound, s"interpreter states after running action $action")(Soundness.isSound(cInterp, aInterp))
      case AssertReturnArithmeticNaN(action) =>
        val aRes = runAAction(action, convertVals)
        val res = runCAction(action)
        checkNaN(res, c.toString)
        assertResult(IsSound.Sound, s"result after running action $action")(Soundness.isSound(res, aRes))
        assertResult(IsSound.Sound, s"interpreter states after running action $action")(Soundness.isSound(cInterp, aInterp))
      case AssertTrap(action: Action, message: String) =>
        val aRes = runAAction(action, convertVals)
        val res = runCAction(action)
        assert(res.isFailing, c.toString)
        assertResult(IsSound.Sound, s"result after running action $action")(Soundness.isSound(res, aRes))
        assertResult(IsSound.Sound, s"interpreter states after running action $action")(Soundness.isSound(cInterp, aInterp))
      case AssertModuleTrap(mod,_) =>
        val aRes = aInstantiate(mod)
        val res = instantiate(mod)
        assert(res.isFailing, c.toString)
        //assertResult(IsSound.Sound, s"result after instantiating module $mod")(Soundness.isSound(res, aRes))
        assertResult(IsSound.Sound, s"interpreter states after instantiating module $mod")(Soundness.isSound(cInterp, aInterp))
      case _: AssertUnlinkable => // skip
      case _: AssertInvalid => // skip
      case _: AssertMalformed => // skip
      case _: AssertExhaustion => // skip
      case action: Action =>
        runAAction(action, convertVals)
        runCAction(action)
      case _: Meta => // skip

  def loadModule(id: Option[String], mod: Module): Unit =
    val cModInst = cInterp.instantiateModule(mod, cImports)
    id match
      case None => // nothing
      case Some(name) => cModules += name -> cModInst
    cCurrent = cModInst
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
        cInterp.failure.fallible {
          cInterp.instantiateModule(mod, cImports)
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

  def runCAction(a: Action): CResult = a match {
    case Invoke(modName, fun, expr) => evalCInvoke(modName, fun, constExprToVals(expr))
    case Get(modName, name) => evalCGet(modName, name)
  }

  def runAAction(a: Action, convertVals: unresolved.Expr => List[TypeAnalysis.Value]): AResult = a match {
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


  def constExprToVals(e: unresolved.Expr): List[ConcreteInterpreter.Value] =
    e.map(constExprToVal).toList

  def constExprToVal(inst: unresolved.Inst): ConcreteInterpreter.Value =
    inst match
      case unresolved.i32.Const(i) => ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int32(i))
      case unresolved.i64.Const(l) => ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int64(l))
      case unresolved.f32.Const(f) => ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f))
      case unresolved.f64.Const(d) => ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d))
      case _ => throw IllegalArgumentException(s"Expected constant instruction but got $inst")

  def constExprToAVals(e: unresolved.Expr): List[TypeAnalysis.Value] =
    e.map(constExprToAVal).toList

  def constExprToAVal(inst: unresolved.Inst): TypeAnalysis.Value =
    inst match
      case unresolved.i32.Const(i) => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Int32(TypeAnalysis.topI32))
      case unresolved.i64.Const(l) => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Int64(TypeAnalysis.topI64))
      case unresolved.f32.Const(f) => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Float32(TypeAnalysis.topF32))
      case unresolved.f64.Const(d) => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Float64(TypeAnalysis.topF64))
      case _ => throw IllegalArgumentException(s"Expected constant instruction but got $inst")

  def constExprToTops(e: unresolved.Expr): List[TypeAnalysis.Value] =
    e.map(constExprToTop).toList

  def constExprToTop(inst: unresolved.Inst): TypeAnalysis.Value =
    inst match
      case unresolved.i32.Const(_) => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Int32(TypeAnalysis.topI32))
      case unresolved.i64.Const(_) => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Int64(TypeAnalysis.topI64))
      case unresolved.f32.Const(_) => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Float32(TypeAnalysis.topF32))
      case unresolved.f64.Const(_) => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Float64(TypeAnalysis.topF64))
      case _ => throw IllegalArgumentException(s"Expected constant instruction but got $inst")

  def isNaN(value: ConcreteInterpreter.Value): Boolean =
    value match
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f)) => f.isNaN
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d)) => d.isNaN
      case _ => false