package sturdy.language.wasm

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import swam.syntax.Module
import swam.text.*


class ParserTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm parser"

  val uri = classOf[ParserTest].getResource("/sturdy/language/wasm").toURI();

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wast")).sorted.foreach { p =>
    it must s"parse ${p.getFileName}" in {
      val path = Path.of(p.toUri)
      parse(path)
    }
  }

def parse(path: Path): Module =
  implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)
  Blocker[IO].use { blocker =>
    for {
      compiler <- Compiler[IO](blocker)
      mod <- compiler.compile(path, blocker)
    } yield mod
  }.unsafeRunSync()