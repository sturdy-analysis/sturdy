package sturdy.language.minijava

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.minijava.Parser.LanguageKeywords.KRETURN
import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import Parser.*

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

class ParserTest extends AnyFlatSpec, Matchers:
  behavior of "MiniJava parser"

  val uri = classOf[ParserTest].getResource("/sturdy/language/minijava").toURI();

  Files.list(Paths.get(uri)).toScala(List).sorted.filter(p => p.toString.endsWith(".minijava")).foreach { p =>
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
