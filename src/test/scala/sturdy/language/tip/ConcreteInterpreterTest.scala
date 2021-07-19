package sturdy.language.tip

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.whilelang.ConcreteInterpreter.*
import sturdy.language.whilelang.ConcreteInterpreter.Value.*

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Success, Try, Failure}

class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:
  def parse(s: String): Either[P.Error, Program] =
    Parser.program.parseAll(s)

  "TIP interpreter" should "run all example files" in {
    val uri = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tip").toURI();
    val tipDir = Paths.get(uri)
    Files.list(tipDir).toScala(Iterator).filter(_.toString.endsWith(".tip")).foreach { p =>
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val program = parse(sourceCode).getOrElse(throw new IllegalStateException())
      if (program.funs.exists(_.name == "main")) {
        print(s"${p.getFileName} prints: ")
        val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
        Try(interp.execute(program)) match
          case Success(_) => println(interp.effectOps.getPrinted)
          case Failure(e) => println(e)
      } else {
        println(s"${p.getFileName}: no main function")
      }
    }
  }
