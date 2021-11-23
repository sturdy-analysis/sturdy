package sturdy.language.wasm

import cats.effect.IO

import java.nio.file.Path
import cats.effect.Blocker
import cats.effect.ContextShift
import fastparse.ParserInputSource
import org.scalatest.Assertions.assert
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.text.Command
import swam.text.Compiler
import swam.text.parser.TestScriptParser
import swam.text.unresolved
import swam.validation.Validator

import java.io.FileInputStream

object Parsing:
  def fromText(path: Path): Module =
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    Blocker[IO].use { blocker =>
      for {
        compiler <- Compiler[IO](blocker)
        mod <- compiler.compile(path, blocker)
      } yield mod
    }.unsafeRunSync()

  def fromBinary(path: Path): Module =
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    Blocker[IO].use { blocker =>
      for {
        validator <- Validator[IO](blocker)
        loader = new ModuleLoader[IO]()
        binaryParser = new ModuleParser[IO](validator)
        mod <- binaryParser.parse(loader.sections(path, blocker))
      } yield mod
    }.unsafeRunSync()

  def fromBytes(bytes: Array[Byte]): Module =
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    Blocker[IO].use { blocker =>
      for {
        validator <- Validator[IO](blocker)
        loader = new ModuleLoader[IO]()
        binaryParser = new ModuleParser[IO](validator)
        mod <- binaryParser.parse(loader.sections(bytes))
      } yield mod
    }.unsafeRunSync()

  def fromUnresolved(mod: unresolved.Module): Module =
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    Blocker[IO].use { blocker =>
      for {
        compiler <- Compiler[IO](blocker)
        mod <- compiler.compile(mod)
      } yield mod
    }.unsafeRunSync()

  def testscript(path: Path): Seq[Command] =
    val script = _root_.fastparse.parse(new FileInputStream(path.toFile), TestScriptParser.script(_))
    assert(script.isSuccess)
    script.get.value