package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.language.whilelang.ConcreteInterpreter.*
import sturdy.language.whilelang.ConcreteInterpreter.Value.*

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class SignAnalysisTest extends AnyFlatSpec, Matchers:
  "TIP sign analysis" should "runs all example files" in {
    val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip").toURI();
    val tipDir = Paths.get(uri)
    Files.list(tipDir).toScala(Iterator).filter(_.toString.endsWith(".tip")).foreach { p =>
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val program = Parser.parse(sourceCode)
      if (program.funs.exists(_.name == "main")) {
        print(s"${p.getFileName} prints: ")
        val analysis = SignAnalysis(Map(), Map(), 100)
        Try(analysis.execute(program)) match
          case Success(_) => println(analysis.effectOps.getPrinted)
          case Failure(e) => println(e)
      } else {
        println(s"${p.getFileName}: no main function")
      }
    }
  }
