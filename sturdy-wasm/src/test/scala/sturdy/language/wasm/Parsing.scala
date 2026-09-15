package sturdy.language.wasm

import cats.effect.IO

import java.nio.file.Path
import cats.effect.Blocker
import cats.effect.ContextShift
import cats.effect.Timer
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
import java.util.concurrent.TimeoutException
import scala.concurrent.duration.Duration
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

  def fromString(module: String): Module =
    implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    implicit val timer: Timer[IO] = cats.effect.IO.timer(scala.concurrent.ExecutionContext.global)
    try {
      Blocker[IO].use { blocker =>
        for {
          compiler <- Compiler[IO](blocker)
          mod <- compiler.compile(module)
        } yield mod
      }.timeout(FiniteDuration(10, "s")).unsafeRunSync()
    } catch {
      case e: TimeoutException => throw new WasmParseError(s"Parsing of $module timed out")
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
      }.timeout(FiniteDuration(10000, "s")).unsafeRunSync()
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
      }.timeout(FiniteDuration(10000, "s")).unsafeRunSync()
    } catch {
      case e: TimeoutException => throw new WasmParseError(s"Parsing of ${mod.id} timed out")
    }

  def offsetToLineCol(input: String, offset: Int): (Int, Int) = {
    val lines = input.take(offset).split("\n")
    val line = lines.length
    val col = if (lines.isEmpty) 1 else lines.last.length + 1
    (line, col)
  }

  def testscript(path: Path): Seq[Command] = {
    import java.nio.file.Files
    import java.nio.charset.StandardCharsets

    val bytes = Files.readAllBytes(path)
    val input = new String(bytes, StandardCharsets.UTF_8)

    val script = fastparse.parse(input, TestScriptParser.script(_))
    script match {
      case f: fastparse.Parsed.Failure =>
        println(f.trace().longMsg)
        val (line, col) = offsetToLineCol(input, f.index)
        val lines = input.split("\n")
        val contextLine = lines.lift(line - 1).getOrElse("")
        val start = (col - 200).max(0)
        val end = (col + 200).min(contextLine.length)
        println(s"Context around error (line $line, col $col):")
        println(contextLine.slice(start, end))
      case _ =>
    }
    assert(script.isSuccess)
    script.get.value
  }