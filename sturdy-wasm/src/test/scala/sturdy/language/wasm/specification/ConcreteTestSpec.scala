package sturdy.language.wasm.specification

import org.scalatest.Assertions.*
import org.scalatest.compatible
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.ControlEventChecker
import sturdy.effect.failure.CFallible
import sturdy.language.wasm.ConcreteInterpreter.{RefValue, Value, constExprToVals, eqVals}
import sturdy.language.wasm.generic.ExternalValue.Global
import sturdy.language.wasm.generic.{ExternalValue, FrameData, ModuleInstance}
import sturdy.language.wasm.{ConcreteInterpreter, Parsing}
import swam.ReferenceType.{ExternRef, FuncRef}
import swam.SwamException
import swam.syntax.Module
import swam.text.*
import swam.text.unresolved.SomeId

import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.jdk.StreamConverters.*


class ConcreteTestSpec extends AnyFlatSpec, Matchers:
  behavior of "TestScript interpreter"

  val pathSpectest: Path = Paths.get(this.getClass.getResource("/sturdy/language/wasm/spectest.wast").toURI)
  val uriWasm1: URI = this.getClass.getResource("/sturdy/language/wasm/spec-test-suite-wasm1").toURI
  val uriWasm2: URI = this.getClass.getResource("/sturdy/language/wasm/spec-test-suite-wasm2/simd").toURI

  val spectest: Module = Parsing.fromText(pathSpectest)

  val EXCLUDE_MEM_GROW = false

  /*Files.list(Paths.get(uriWasm1)).toScala(List).filter(p => p.toString.endsWith(".wast")).filter(p => {
    !(EXCLUDE_MEM_GROW && p.getFileName.toString.contains("memory_grow.wast"))
  }).sorted.foreach { p =>
    it must s"execute WASM1 script ${p.getFileName}" in {
      println(s"Executing TestScript interpreter on WASM1 script ${p.getFileName}")
      val script = Parsing.testscript(p)
      val interp = ConcreteTestSpecInterpreter(Some(spectest))
      interp.run(script)
    }
  }*/

  Files.list(Paths.get(uriWasm2)).toScala(List).filter(p => p.toString.endsWith(".wast")).filter(p => {
    !(EXCLUDE_MEM_GROW && p.getFileName.toString.contains("memory_grow.wast"))
  }).sorted.foreach { p =>
    it must s"execute WASM2 script ${p.getFileName}" in {
      println(s"Executing TestScript interpreter on WASM2 script ${p.getFileName}")
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
      case AssertInvalid(m, _) =>
        assertThrows[SwamException] {
          instantiate(m)
        }
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
      case BinaryModule(id,s) => throw new SwamException("instantiation of binary modules not yet implemented.")
      case QuotedModule(id, s) => throw new SwamException("instantiation of quoted modules not yet implemented.")

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


def isNaN(value: Value): Boolean =
  value match
    case Value.Num(ConcreteInterpreter.NumValue.Float32(f)) => f.isNaN
    case Value.Num(ConcreteInterpreter.NumValue.Float64(d)) => d.isNaN
    case _ => false