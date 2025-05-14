package sturdy.language.wasm.specification

import org.scalatest.Assertions.*
import org.scalatest.compatible
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.ControlEventChecker
import sturdy.effect.failure.CFallible
import sturdy.language.wasm.ConcreteInterpreter.Value
import sturdy.language.wasm.{ConcreteInterpreter, Parsing}
import sturdy.language.wasm.generic.ExternalValue.Global
import sturdy.language.wasm.generic.{ExternalValue, FrameData, ModuleInstance}
import swam.ReferenceType.{ExternRef, FuncRef}
import swam.syntax.Module
import swam.text.*
import swam.text.unresolved.SomeId

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.jdk.StreamConverters.*


class ConcreteTestSpec extends AnyFlatSpec, Matchers:
  behavior of "TestScript interpreter"

  val pathSpectest: Path = Paths.get(this.getClass.getResource("/sturdy/language/wasm/spectest.wast").toURI)
  val uri: URI = this.getClass.getResource("/sturdy/language/wasm/spec-test-suite-wasm1").toURI

  val spectest: Module = Parsing.fromText(pathSpectest)

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wast")).sorted.foreach { p =>
    it must s"execute ${p.getFileName}" in {
      println(s"Executing TestScript interpreter on ${p.getFileName}")
      val script = Parsing.testscript(p)
      val interp = ConcreteTestSpecInterpreter(Some(spectest))
      interp.run(script)
    }
  }

class ConcreteTestSpecInterpreter(spectest: Option[Module] = None):
  val interp = new ConcreteInterpreter.Instance(FrameData.empty, Iterable.empty)
  interp.addControlObserver(new ControlEventChecker)
  val modules: mutable.Map[String, ModuleInstance] = mutable.Map()
  var current: ModuleInstance = null
  var imports: Map[String, ModuleInstance] = Map()
  
  spectest.foreach{ mod => 
    val modInst = interp.initializeModule(mod)
    current = modInst
    imports += "spectest" -> modInst
  }

  type Result = CFallible[List[Value]]

  def eqVals(vs1: List[Value], vs2: List[Value]): Boolean =
    vs1.size == vs2.size && vs1.zip(vs2).forall {
      case (Value.Num(ConcreteInterpreter.NumValue.Int32(i1)), Value.Num(ConcreteInterpreter.NumValue.Int32(i2))) => i1 == i2
      case (Value.Num(ConcreteInterpreter.NumValue.Int64(l1)), Value.Num(ConcreteInterpreter.NumValue.Int64(l2))) => l1 == l2
      case (Value.Num(ConcreteInterpreter.NumValue.Float32(f1)), Value.Num(ConcreteInterpreter.NumValue.Float32(f2))) => f1.isNaN && f2.isNaN || f1 == f2
      case (Value.Num(ConcreteInterpreter.NumValue.Float64(d1)), Value.Num(ConcreteInterpreter.NumValue.Float64(d2))) => d1.isNaN && d2.isNaN || d1 == d2
      case (Value.Ref(ConcreteInterpreter.RefValue.FuncNull), Value.Ref(ConcreteInterpreter.RefValue.FuncNull)) => true
      case (Value.Ref(ConcreteInterpreter.RefValue.ExternNull), Value.Ref(ConcreteInterpreter.RefValue.ExternNull)) => true
      case (Value.Ref(ConcreteInterpreter.RefValue.FuncRef(r1)), Value.Ref(ConcreteInterpreter.RefValue.FuncRef(r2))) => r1 == r2
      case (Value.Ref(ConcreteInterpreter.RefValue.ExternRef(r1)), Value.Ref(ConcreteInterpreter.RefValue.ExternRef(r2))) => r1 == r2
      case _ => false
    }

  def run(commands: Seq[Command]): Unit =
    commands.foreach(c => {println(c); eval(c)})

  def getModule(module: Option[String]): ModuleInstance = module match
    case None => current
    case Some(name) => modules(name)

  def eval(c: Command): Unit = c match
      case ValidModule(m) =>
        // validate and compile module
        val mod = Parsing.fromUnresolved(m)
        val id = m.id match
          case SomeId(name) => Some(name)
          case _ => None
        loadModule(id, mod)
      case Register(s, id) =>
        imports += s -> getModule(id)
      case BinaryModule(id, bytes) =>
        val mod = Parsing.fromBytes(bytes)
        loadModule(id, mod)
      case QuotedModule(id, text) =>
        ???
      case AssertReturn(action, expectedRes) =>
        val res = runAction(action)
        assert(!res.isFailing, s"$action failed $res")
        val expected = constExprToVals(expectedRes)
        assert(eqVals(expected, res.get), c.toString + s" but expected $expected != actual ${res.get}")
      case AssertReturnCanonicalNaN(action) =>
        val res = runAction(action)
        checkNaN(res, c.toString)
      case AssertReturnArithmeticNaN(action) =>
        val res = runAction(action)
        checkNaN(res, c.toString)
      case AssertTrap(action: Action, message: String) =>
        val res = runAction(action)
        assert(res.isFailing, c.toString)
      case AssertModuleTrap(mod,_) =>
        val res = instantiate(mod)
        assert(res.isFailing, c.toString)
      case _: AssertUnlinkable => // skip
      case _: AssertInvalid => // skip
      case _: AssertMalformed => // skip
      case _: AssertExhaustion => // skip
      case action: Action => runAction(action)
      case _: Meta => // skip

  def loadModule(id: Option[String], mod: Module): Unit =
    val modInst = interp.initializeModule(mod, imports)
    id match
      case None => // nothing
      case Some(name) => modules += name -> modInst
    current = modInst

  def instantiate(t: TestModule): CFallible[ModuleInstance] =
    t match
      case ValidModule(m) =>
        val mod = Parsing.fromUnresolved(m)
        interp.failure.fallible {
          interp.initializeModule(mod, imports)
        }
      case BinaryModule(id,s) => throw new Error("instantiation of binary modules not yet implemented.")
      case QuotedModule(id, s) => throw new Error("instantiation of quoted modules not yet implemented.")

  def runAction(a: Action): Result = a match {
    case Invoke(modName, fun, expr) => evalInvoke(modName, fun, constExprToVals(expr))
    case Get(modName, name) => evalGet(modName, name)
  }

  def evalInvoke(module: Option[String], fun: String, vals: List[Value]): Result =
    val modInst = getModule(module)
    interp.failure.fallible {
      interp.invokeExported(modInst, fun, vals)
    }

  def evalGet(module: Option[String], name: String): Result =
    val modInst = getModule(module)
    val exp = modInst.exports.find(_._1 == name)
    assert(exp.isDefined, s"export $name not found in ${module.getOrElse("current")}")
    exp.get._2 match
      case Global(addr) =>
        val globalIdx = modInst.globalAddrs.lift(addr).getOrElse(throw new Error(s"Unbound global $addr"))
        val value = interp.getGlobalValue(globalIdx)
        CFallible.Unfailing(List(value))
      case ext =>
        throw new IllegalArgumentException(s"Can only get globals, but $name was $ext")

  def checkNaN(res: Result, clue: String): compatible.Assertion =
    assert(!res.isFailing)
    val resClean: List[Value] = res.get
    assert(resClean.size == 1, clue)
    val h = resClean.head
    assert(isNaN(h), clue)


def constExprToVals(e: unresolved.Expr): List[Value] =
  e.map(constExprToVal).toList

def constExprToVal(inst: unresolved.Inst): Value =
  inst match
    case unresolved.i32.Const(i) => Value.Num(ConcreteInterpreter.NumValue.Int32(i))
    case unresolved.i64.Const(l) => Value.Num(ConcreteInterpreter.NumValue.Int64(l))
    case unresolved.f32.Const(f) => Value.Num(ConcreteInterpreter.NumValue.Float32(f))
    case unresolved.f64.Const(d) => Value.Num(ConcreteInterpreter.NumValue.Float64(d))
    case unresolved.RefNull(t) => t match {
      case FuncRef => Value.Ref(ConcreteInterpreter.RefValue.FuncNull)
      case ExternRef => Value.Ref(ConcreteInterpreter.RefValue.ExternNull)
    }
    case unresolved.RefFunc(x) => x match {
      case Left(r) => Value.Ref(ConcreteInterpreter.RefValue.FuncNull)
      case _ => Value.Ref(ConcreteInterpreter.RefValue.FuncNull)
    }
    case unresolved.RefExtern(x) => x match {
      case Left(r) => Value.Ref(ConcreteInterpreter.RefValue.ExternNull)
      case _ => Value.Ref(ConcreteInterpreter.RefValue.ExternNull)
    }

    case _ => throw IllegalArgumentException(s"Expected constant instruction but got $inst")

def isNaN(value: Value): Boolean =
  value match
    case Value.Num(ConcreteInterpreter.NumValue.Float32(f)) => f.isNaN
    case Value.Num(ConcreteInterpreter.NumValue.Float64(d)) => d.isNaN
    case _ => false