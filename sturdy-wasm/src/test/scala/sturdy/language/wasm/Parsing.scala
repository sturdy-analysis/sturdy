package sturdy.language.wasm

import cats.effect.{Blocker, ContextShift, IO, Timer}
import fastparse.ParserInputSource
import org.scalatest.Assertions.assert
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.text.parser.TestScriptParser
import swam.text.{Command, Compiler, unresolved}
import swam.validation.Validator

import java.io.FileInputStream
import java.nio.file.Path
import java.util.concurrent.TimeoutException
import scala.concurrent.duration.FiniteDuration

object Parsing:
  class WasmParseError(msg: String) extends Exception(msg)

  def fromText(path: Path): Module =
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    implicit val timer: Timer[IO] = cats.effect.IO.timer(scala.concurrent.ExecutionContext.global)
    try {
      Blocker[IO].use { blocker =>
        for {
          compiler <- Compiler[IO](blocker)
          mod <- compiler.compile(path, blocker)
        } yield mod
      }.timeout(FiniteDuration(10, "s")).unsafeRunSync()
    } catch {
      case e: TimeoutException => throw new WasmParseError(s"Parsing of $path timed out")
    }

  def fromBinary(path: Path): Module =
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    implicit val timer: Timer[IO] = cats.effect.IO.timer(scala.concurrent.ExecutionContext.global)
    try {
      Blocker[IO].use { blocker =>
        for {
          validator <- Validator[IO](blocker)
          loader = new ModuleLoader[IO]()
          binaryParser = new ModuleParser[IO](validator)
          mod <- binaryParser.parse(loader.sections(path, blocker))
        } yield mod
      }.timeout(FiniteDuration(10, "s")).unsafeRunSync()
    } catch {
      case e: TimeoutException => throw new WasmParseError(s"Parsing of $path timed out")
    }

  def fromBytes(bytes: Array[Byte]): Module =
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    implicit val timer: Timer[IO] = cats.effect.IO.timer(scala.concurrent.ExecutionContext.global)
    try {
      Blocker[IO].use { blocker =>
        for {
          validator <- Validator[IO](blocker)
          loader = new ModuleLoader[IO]()
          binaryParser = new ModuleParser[IO](validator)
          mod <- binaryParser.parse(loader.sections(bytes))
        } yield mod
      }.timeout(FiniteDuration(10, "s")).unsafeRunSync()
    } catch {
      case e: TimeoutException => throw new WasmParseError(s"Parsing timed out")
    }

  def fromUnresolved(mod: unresolved.Module): Module =
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    implicit val timer: Timer[IO] = cats.effect.IO.timer(scala.concurrent.ExecutionContext.global)
    try {
      Blocker[IO].use { blocker =>
        for {
          compiler <- Compiler[IO](blocker)
          mod <- compiler.compile(mod)
        } yield mod
      }.timeout(FiniteDuration(10, "s")).unsafeRunSync()
    } catch {
      case e: TimeoutException => throw new WasmParseError(s"Parsing of ${mod.id} timed out")
    }

  def testscript(path: Path): Seq[Command] =
    val script = _root_.fastparse.parse(new FileInputStream(path.toFile), TestScriptParser.script(_))
    assert(script.isSuccess)
    script.get.value