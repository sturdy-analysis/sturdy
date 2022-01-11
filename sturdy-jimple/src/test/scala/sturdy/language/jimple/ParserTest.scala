package sturdy.language.jimple


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.jimple.Parser.LanguageKeywords.KRETURN
import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import Parser.*

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

class ParserTest extends AnyFlatSpec, Matchers:
  behavior of "Jimple parser"

  val uri = classOf[ParserTest].getResource("/sturdy/language/jimple").toURI();

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".jimple")).sorted.foreach { p =>
    it must s"execute ${p.getFileName}" in {
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val tree = parse(sourceCode)
      println(tree)
      assert(tree.isRight)
    }
  }

  def parse(s: String): Either[P.Error, Class] =
    Parser.classes.parseAll(s)
