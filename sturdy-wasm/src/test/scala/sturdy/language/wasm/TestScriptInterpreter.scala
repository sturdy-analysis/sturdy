package sturdy.language.wasm

import cats.effect.{IO, Blocker}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.{CFailureException, CFallible}
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.generic.ExternalValue.Global
import sturdy.language.wasm.generic.ModuleInstance
import ConcreteInterpreter.Value

import java.nio.file.{Path, Paths, Files}
import scala.io.Source
import scala.jdk.StreamConverters.*
import swam.syntax.Module
import swam.text.*
import org.scalatest.Assertions.*
import sturdy.language.wasm.generic.ExternalValue
import sturdy.language.wasm.generic.ExternalValue
import sturdy.language.wasm.generic.ExternalValue
import swam.text.unresolved.FreshId
import swam.text.unresolved.NoId
import swam.text.unresolved.SomeId

import scala.collection.mutable


class TestScriptInterpreter:
  val interp = ConcreteInterpreter(FrameData.empty, Iterable.empty)
  val modules: mutable.Map[String, ModuleInstance[Value]] = mutable.Map()
  var current: ModuleInstance[Value] = null

  type Result = CFallible[List[Value]]

  def run(commands: Seq[Command]): Unit =
    commands.map(eval)

  def getModule(module: Option[String]): ModuleInstance[Value] = module match
    case None => current
    case Some(name) => modules(name)

  def eval(c: Command): Unit = c match
      case ValidModule(m) =>
        // validate and compile module
        val mod = compileUnresovedModule(m)
        // initialize module
        val modInst = interp.initializeModule(mod)
        m.id match
          case NoId | FreshId(_) => // nothing
          case SomeId(name) => modules += name -> modInst
        current = modInst
      case Register(s, id) =>
        modules += s -> getModule(id)
      case BinaryModule(id, bytes) =>
        // TODO
      case QuotedModule(id, text) =>
        // TODO
      case AssertReturn(action, expectedRes) =>
        val res = runAction(action)
        assertResult(CFallible.Unfailing(constExprToVals(expectedRes)), c.toString)(res)
      case AssertReturnCanonicalNaN(action) =>
        val res = runAction(action)
        checkNaN(res, c.toString)
      case AssertReturnArithmeticNaN(action) =>
        val res = runAction(action)
        checkNaN(res, c.toString)
      case AssertTrap(action: Action, message: String) =>
        val res = runAction(action)
        assert(res.isFailing, c.toString)
      case _: AssertInvalid => // skip
      case _: AssertMalformed => // skip
      case _: AssertUnlinkable => // skip
      case _: AssertModuleTrap => // skip
      case _: AssertExhaustion => // skip
      case action: Action => runAction(action)

  def runAction(a: Action): Result = a match {
    case Invoke(modName, fun, expr) => evalInvoke(modName, fun, constExprToVals(expr))
    case Get(modName, name) => evalGet(modName, name)
  }

  def evalInvoke(module: Option[String], fun: String, vals: List[Value]): Result =
    val modInst = getModule(module)
    interp.effects.fallible {
      interp.invokeExported(modInst, fun, vals)
    }

  def evalGet(module: Option[String], name: String): Result =
    val modInst = getModule(module)
    val exp = modInst.exports.find(_._1 == name)
    assert(exp.isDefined, s"export $name not found in ${module.getOrElse("current")}")
    exp.get._2 match
      case Global(addr) =>
        val value = modInst.globals(addr).value
        CFallible.Unfailing(List(value))
      case ext =>
        throw new IllegalArgumentException(s"Can only get globals, but $name was $ext")

  def checkNaN(res: Result, clue: String) =
    assert(!res.isFailing)
    val resClean: List[Value] = res.get
    assert(resClean.size == 1, clue)
    val h = resClean.head
    assert(isNaN(h), clue)


def constExprToVals(e: unresolved.Expr): List[Value] =
  e.map(constExprToVal).toList

def constExprToVal(inst: unresolved.Inst): Value =
  inst match
    case unresolved.i32.Const(i) => Value.Int32(i)
    case unresolved.i64.Const(l) => Value.Int64(l)
    case unresolved.f32.Const(f) => Value.Float32(f)
    case unresolved.f64.Const(d) => Value.Float64(d)
    case _ => throw IllegalArgumentException(s"Expcted constant instruction but got $inst")


def compileUnresovedModule(mod: unresolved.Module): Module =
  implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)
  Blocker[IO].use { blocker =>
    for {
      compiler <- Compiler[IO](blocker)
      mod <- compiler.compile(mod)
    } yield mod
  }.unsafeRunSync()

//def compileBinaryModule(bytes: Array[Byte]): Module =
//  implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)
//  Blocker[IO].use { blocker =>
//    for {
//      compiler <- Compiler[IO](blocker)
//      mod <- compiler.compile(mod)
//    } yield mod
//  }.unsafeRunSync()

def isNaN(value: Value): Boolean =
  value match
    case Value.Float32(f) => f.isNaN
    case Value.Float64(d) => d.isNaN
    case _ => false