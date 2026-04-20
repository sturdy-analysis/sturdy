package sturdy.language.wasm.specification

import cats.effect.{Blocker, IO}
import org.scalacheck.Gen
import org.scalacheck.Test.Parameters
import org.scalatest
import org.scalatest.Assertions.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import sturdy.control.{ControlEventChecker, PrintingControlObserver}
import sturdy.effect.failure.CFallible
import sturdy.effect.failure.{AFallible, given}
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.analyses.{IntervalAnalysis, WasmConfig}
import sturdy.language.wasm.analyses.IntervalAnalysisSoundness.given
import sturdy.language.wasm.generic.ExternalValue.Global
import sturdy.language.wasm.generic.{ExternalValue, FrameData, ModuleInstance, WasmFailure}
import sturdy.values.integer.{NumericInterval, given}
import sturdy.values.ordering.EqOps
import sturdy.values.{*, given}
import sturdy.{IsSound, Soundness}
import sturdy.{*, given}
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.analyses.CallSites
import sturdy.language.wasm.analyses.FixpointConfig
import sturdy.language.wasm.analyses.Insensitive
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.util.GenInterval.genInterval
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.text.*
import swam.text.unresolved.FreshId
import swam.text.unresolved.NoId
import swam.text.unresolved.SomeId
import swam.validation.Validator

import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.collection.mutable
import scala.io.Source
import scala.jdk.StreamConverters.*

class IntervalAnalysisTestSpec extends AnyFlatSpec, Matchers:
  behavior of "TestScript interval analysis"

  val pathSpectest = Paths.get(this.getClass.getResource("/sturdy/language/wasm/spectest.wast").toURI)
  val uriWasm1: URI = this.getClass.getResource("/sturdy/language/wasm/spec-test-suite-wasm1").toURI
  val uriWasm2: URI = this.getClass.getResource("/sturdy/language/wasm/spec-test-suite-wasm2").toURI
  val uriSIMD: URI = this.getClass.getResource("/sturdy/language/wasm/spec-test-suite-wasm2/simd").toURI

  val spectest = Parsing.fromText(pathSpectest)


  def analyses: IterableOnce[() => IntervalAnalysis.Instance] =
    Iterator(
      () => new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(), ctx = Insensitive)),
//      () => new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Innermost(StackConfig.StackedCfgNodes())), ctx = Insensitive)),
//      () => new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Innermost(false)), ctx = Insensitive)),
//      () => new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Outermost(true)), ctx = Insensitive)),
//      () => new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Outermost(false)), ctx = Insensitive)),
//      () => new IntervalAnalysisSturdyInstance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Innermost), ctx = CallSites(1))),
//      () => new IntervalAnalysisSturdyInstance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = fix.iter.Config.Topmost), ctx = CallSites(1))),
    )

  Fixpoint.DEBUG = false

  def runTests(uri: URI, msg: String => String): Unit =
    Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wast")).sorted.foreach { p =>
      for (aInterp <- analyses) {
        it must msg(p.getFileName.toString) in {
          val script = Parsing.testscript(p)
          IntervalAnalysisTestSpecInterpreter(Some(spectest), aInterp(), testAbstractInputs = false).run(script)
          IntervalAnalysisTestSpecInterpreter(Some(spectest), aInterp(), testAbstractInputs = true).run(script)
        }
      }
    }

  runTests(uriWasm1, s => s"execute WASM1 script $s")
  runTests(uriWasm2, s => s"execute WASM2 script $s")
  runTests(uriSIMD, s => s"execute SIMD script $s")

class IntervalAnalysisTestSpecInterpreter(spectest: Option[Module] = None, aInterp: IntervalAnalysis.Instance, testAbstractInputs: Boolean = false):
  type CValue = ConcreteInterpreter.Value
  type AValue = IntervalAnalysis.Value

  val cInterp = new ConcreteInterpreter.Instance(FrameData.empty, Iterable.empty)
  aInterp.addControlObserver(new ControlEventChecker)
//  aInterp.addControlObserver(new PrintingControlObserver()(println))

  val cModules: mutable.Map[String, ModuleInstance] = mutable.Map()
  val aModules: mutable.Map[String, ModuleInstance] = mutable.Map()
  var cCurrent: ModuleInstance = null
  var aCurrent: ModuleInstance = null
  var cImports: Map[String, ModuleInstance] = Map()
  var aImports: Map[String, ModuleInstance] = Map()

  given scalaCheckParameters: Parameters = Parameters.default.withMinSuccessfulTests(1)

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
    commands.map(eval)

  def getCModule(module: Option[String]): ModuleInstance = module match
    case None => cCurrent
    case Some(name) => cModules(name)

  def getAModule(module: Option[String]): ModuleInstance = module match
    case None => aCurrent
    case Some(name) => aModules(name)

  def eval(c: Command): Unit = {
    println(c)
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
        val mod = Parsing.fromString(text)
        loadModule(id, mod)
      case AssertReturn(action, expectedRes) =>
        val res = runCAction(action)
        assert(!res.isFailing)
        val expected = constExprToVals(expectedRes)
        assert(eqVals(expected, res.get), c.toString + s" but $expected != ${res.get}")
        runAAction(action) { aRes =>
          assertResult(IsSound.Sound, s"result after running action $action")(Soundness.isSound(res, aRes))
          assertResult(IsSound.Sound, s"interpreter states after running action $action")(Soundness.isSound(cInterp, aInterp))
        }
      case AssertReturnCanonicalNaN(action) =>
        val res = runCAction(action)
        checkNaN(res, c.toString)
        runAAction(action) { aRes =>
          assertResult(IsSound.Sound, s"result after running action $action")(Soundness.isSound(res, aRes))
          assertResult(IsSound.Sound, s"interpreter states after running action $action")(Soundness.isSound(cInterp, aInterp))
        }
      case AssertReturnArithmeticNaN(action) =>
        val res = runCAction(action)
        checkNaN(res, c.toString)
        runAAction(action) { aRes =>
          assertResult(IsSound.Sound, s"result after running action $action")(Soundness.isSound(res, aRes))
          assertResult(IsSound.Sound, s"interpreter states after running action $action")(Soundness.isSound(cInterp, aInterp))
        }
      case AssertTrap(action: Action, message: String) =>
        val res = runCAction(action)
        assert(res.isFailing, c.toString)
        runAAction(action) { aRes =>
          assertResult(IsSound.Sound, s"result after running action $action")(Soundness.isSound(res, aRes))
          assertResult(IsSound.Sound, s"interpreter states after running action $action")(Soundness.isSound(cInterp, aInterp))
        }
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
        runAAction(action) { _ => assert(true) }
        runCAction(action)
      case _: Meta =>
  } // skip

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

  def runAAction(a: Action)(checkResult: AResult => org.scalatest.Assertion): scalatest.Assertion = a match {
    case Invoke(modName, fun, expr) =>
      if(testAbstractInputs) {
        forAll(genAVals(expr)) { avals =>
          checkResult(evalAInvoke(modName, fun, avals))
        }
      } else {
        checkResult(evalAInvoke(modName, fun, constantAVals(expr)))
      }
    case Get(modName, name) =>
      checkResult(evalAGet(modName, name))
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

  def constExprToVal(inst: unresolved.Inst): ConcreteInterpreter.Value = {
    import ConcreteInterpreter.Value.*
    import ConcreteInterpreter.NumValue.*
    inst match
      case unresolved.i32.Const(i) => Num(Int32(i))
      case unresolved.i64.Const(l) => Num(Int64(l))
      case unresolved.f32.Const(f) => Num(Float32(f))
      case unresolved.f64.Const(d) => Num(Float64(d))
      case _ => throw IllegalArgumentException(s"Expected constant instruction but got $inst")
  }

  def constantAVals(e: unresolved.Expr): List[IntervalAnalysis.Value] = e.map(constantAVal).toList

  def constantAVal(inst: unresolved.Inst): IntervalAnalysis.Value = Abstractly(constExprToVal(inst))

  def genAVals(e: unresolved.Expr): Gen[List[IntervalAnalysis.Value]] =
    e match
      case Nil => Gen.const(List[IntervalAnalysis.Value]())
      case inst :: rest =>
        for {
          aVal <- genAVal(inst)
          aRest <- genAVals(rest)
        } yield (aVal :: aRest)

  def genAVal(inst: unresolved.Inst): Gen[IntervalAnalysis.Value] = {
    import IntervalAnalysis.Value.*
    import IntervalAnalysis.NumValue.*
    inst match {
      case unresolved.i32.Const(i) =>
        for {
          iv <- genInterval(included = i, minValue = Int.MinValue, maxValue = Int.MaxValue, specials = List(Int.MinValue, -1, 0, 1, Int.MaxValue)*)
        } yield Num(Int32(NumericInterval(iv.low, iv.high)))
      case unresolved.i64.Const(l) =>
        for {
          iv <- genInterval(included = l, minValue = Long.MinValue, maxValue = Long.MaxValue, specials = List(Long.MinValue, -1, 0, 1, Long.MaxValue)*)
        } yield Num(Int64(NumericInterval(iv.low, iv.high)))
      case unresolved.f32.Const(f) =>
        for {
          absF <- Gen.oneOf(List(Topped.Actual(f), Topped.Top))
        } yield Num(Float32(absF))
      case unresolved.f64.Const(d) =>
        for {
          absD <- Gen.oneOf(List(Topped.Actual(d), Topped.Top))
        } yield Num(Float64(absD))
      case _ => throw IllegalArgumentException(s"Expected constant instruction but got $inst")
    }
  }

  def isNaN(value: ConcreteInterpreter.Value): Boolean =
    value match
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f)) => f.isNaN
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d)) => d.isNaN
      case _ => false