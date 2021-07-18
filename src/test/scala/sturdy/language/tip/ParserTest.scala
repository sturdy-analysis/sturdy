package sturdy.language.tip

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.whilelang.ConcreteInterpreter.*
import sturdy.language.whilelang.ConcreteInterpreter.Value.*

import cats.parse.{Parser0 => P0, Parser => P, Numbers}
import Parser._

class ParserTest extends AnyFlatSpec, Matchers:
  def parse(s: String): Program =
    Parser.program.parseAll(s) match
      case Right(p) => p
      case Left(err) => throw new IllegalStateException(err.toString)

  "TIP parser" should "run ex1" in {
    println(whitespace.parseAll(" "))
    println(whitespaces0.parseAll("  "))
    println(whitespaces0.parseAll(""))
    println(P.char('a').parseAll("a"))
    println(P.string(KRETURN).parseAll("return"))
    println(keyword(KRETURN).parseAll(" return"))
    println(expression.parseAll("0"))
    println((keyword(KRETURN) *> expression <* semi).parseAll("return 0;"))
    println(identifier.parseAll("foo"))
    println((identifier ~ inParens(list(identifier))).parseAll("foo(x)"))

    val s =
      """foo(x) {
        |  return 0;
        |}
        |""".stripMargin
    parse(s)
  }
