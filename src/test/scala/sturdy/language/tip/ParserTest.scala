package sturdy.language.tip

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.whilelang.ConcreteInterpreter.*
import sturdy.language.whilelang.ConcreteInterpreter.Value.*
import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import Parser.*

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

class ParserTest extends AnyFlatSpec, Matchers:
  def parse(s: String): Either[P.Error, Program] =
    Parser.program.parseAll(s)

  "TIP parser" should "parse all example files" in {
    val uri = classOf[ParserTest].getResource("/sturdy/language/tip").toURI();
    val tipDir = Paths.get(uri)
    Files.list(tipDir).toScala(Iterator).filter(_.toString.endsWith(".tip")).foreach { p =>
      println(s"Parsing $p")
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val tree = parse(sourceCode)
      if (tree.isLeft)
      println(tree)
      assert(tree.isRight)
    }
  }
