package sturdy.language.wasm

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.wasm.generic.GenericInterpreter.FrameData
import sturdy.language.wasm.generic.ModuleInstance

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import swam.syntax.Module
import swam.text.*

import org.scalatest.Assertions.*

import scala.collection.mutable.ArrayBuffer


class TestScriptInterpreter:
  val interp = ConcreteInterpreter(FrameData(0, null), Iterable.empty)
  val modules: ArrayBuffer[ModuleInstance[ConcreteInterpreter.Value]] = ArrayBuffer.empty

  def run(commands: Seq[Command]) =
    commands.map(eval)

  def eval(c: Command) = c match
    case ValidModule(m) =>
      // validate and compile module
      val mod = compileUnresovedModule(m)
      // initialize module
      modules += interp.initializeModule(mod)
    case AssertReturn(invoke: Invoke, expectedRes) =>
      val res = evalInvoke(invoke)
      assertResult(constExprToVals(expectedRes))(res)
    case _ => //skip for now

  def evalInvoke(invoke: Invoke): List[ConcreteInterpreter.Value] =
    val modInst = modules.last
    interp.invokeExported(modInst, invoke.s, constExprToVals(invoke.inst))

  def constExprToVals(e: unresolved.Expr): List[ConcreteInterpreter.Value] =
    e.map(constExprToVal).toList

  def constExprToVal(inst: unresolved.Inst): ConcreteInterpreter.Value =
    inst match
      case unresolved.i32.Const(i) => ConcreteInterpreter.Value.Int32(i)
      case unresolved.i64.Const(l) => ConcreteInterpreter.Value.Int64(l)
      case unresolved.f32.Const(f) => ConcreteInterpreter.Value.Float32(f)
      case unresolved.f64.Const(d) => ConcreteInterpreter.Value.Float64(d)
      case _ => throw IllegalArgumentException(s"Expcted constant instruction but got $inst")


def compileUnresovedModule(mod: unresolved.Module): Module =
  implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)
  Blocker[IO].use { blocker =>
    for {
      compiler <- Compiler[IO](blocker)
      mod <- compiler.compile(mod)
    } yield mod
  }.unsafeRunSync()

