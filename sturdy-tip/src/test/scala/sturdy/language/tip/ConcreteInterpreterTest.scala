package sturdy.language.tip

import cats.parse.{Numbers, Parser0 as P0, Parser as P}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.analysis.SignAnalysisTest

import java.nio.file.Path
import java.nio.file.{Paths, Files}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:

  behavior of "Tip concrete interpreter"

  val uri = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tip").toURI();

  Files.list(Paths.get(uri)).toScala(List).sorted.filter(p => p.toString.endsWith(".tip")).foreach { p =>
    it must s"execute ${p.getFileName}" in {
      runFile(p)
    }
  }

  def runFile(p: Path): Unit =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
//      print(s"${p.getFileName}")
      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      interp.effectOps.fallible(interp.execute(program))
//      Try(interp.execute(program)) match
//        case Success(_) => println(" prints: " + interp.effectOps.getPrinted)
//        case Failure(e) => println(" errors: " + e)
    } else {
//      println(s"${p.getFileName}: no main function")
    }

object RunConcreteInterpreter extends App:
  def runFile(p: Path) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
    (interp.effectOps.fallible(interp.execute(program)), interp.effectOps)

  val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip/interpreter_test.tip").toURI();
  val (res, effects) = runFile(Paths.get(uri))
  println(res)
  println(effects.getCallFrame)
  println(effects.getStore)
