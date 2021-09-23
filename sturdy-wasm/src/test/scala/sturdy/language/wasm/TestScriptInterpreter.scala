package sturdy.language.wasm

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.{CFailureException, CFallible}
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.generic.ModuleInstance
import ConcreteInterpreter.Value

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import swam.syntax.Module
import swam.text.*
import org.scalatest.Assertions.*

import scala.collection.mutable.ArrayBuffer


class TestScriptInterpreter:
  val interp = ConcreteInterpreter(FrameData.empty, Iterable.empty)
  val modules: ArrayBuffer[ModuleInstance[Value]] = ArrayBuffer.empty

  def run(commands: Seq[Command]) =
    commands.map(eval)

  def eval(c: Command) = c match
    case ValidModule(m) =>
      // validate and compile module
      val mod = compileUnresovedModule(m)
      // initialize module
      modules += interp.initializeModule(mod)
    case BinaryModule(id, bytes) =>
      // TODO
    case QuotedModule(id, text) =>
      // TODO
    case AssertReturn(invoke: Invoke, expectedRes) =>
      val res = evalInvoke(invoke)
      assertResult(CFallible.Unfailing(constExprToVals(expectedRes)))(res)
    case AssertReturnCanonicalNaN(invoke: Invoke) => checkNaN(invoke)
    case AssertReturnArithmeticNaN(invoke: Invoke) => checkNaN(invoke)
    case AssertTrap(invoke: Invoke, message: String) =>
      val res = evalInvoke(invoke)
      // TODO: do we check for the right failure kind?
      assert(res.isFailing)
    case _ => //skip for now

  def evalInvoke(invoke: Invoke): CFallible[Iterable[Value]] =
    val modInst = modules.last
    interp.effects.fallible {
      interp.invokeExported(modInst, invoke.s, constExprToVals(invoke.inst))
    }
    
  def checkNaN(invoke: Invoke) =
    val res = evalInvoke(invoke)
    assert(!res.isFailing)
    val resClean: List[Value] = res.asInstanceOf[CFallible.Unfailing[List[Value]]].t
    assert(resClean.size == 1)
    val h = resClean.head
    assert(isNaN(h))


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