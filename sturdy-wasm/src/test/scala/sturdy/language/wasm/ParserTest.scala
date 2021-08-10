package sturdy.language.wasm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import cats.parse.Numbers
import cats.parse.Parser0 as P0
import cats.parse.Parser as P

import java.nio.file.Files
import java.nio.file.Paths
import scala.io.Source
import scala.jdk.StreamConverters.*

class ParserTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm parser"

  val uri = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tip").toURI();

  Files.list(Paths.get(uri)).toScala(List).sorted.filter(p => p.toString.endsWith(".tip")).foreach { p =>
    it must s"execute ${p.getFileName}" in {
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val tree = parse(sourceCode)
      assert(tree.isRight)
    }
  }

  def parse(s: String): Either[P.Error, Program] =
    Parser.program.parseAll(s)
