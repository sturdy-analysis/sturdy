package sturdy.language.tip_xdai

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.tip_xdai.Parser.*
import sturdy.language.tip_xdai.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip_xdai.arithmetic.concrete.IntValue

import java.nio.file.Path
import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:

  behavior of "Tip concrete interpreter"

  val uri = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tip").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    p.toString.endsWith(".tip")).sorted.foreach { p =>
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
      val interp = ConcreteInterpreter(() => IntValue(0))
      interp.failure.fallible(interp.execute(program))
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
    val interp = ConcreteInterpreter(() => IntValue(0))
    (interp.failure.fallible(interp.execute(program)), interp)

  val uri = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tip/interpreter_test.tip").toURI;
  val (res, interp) = runFile(Paths.get(uri))
  println(res)
  println(interp.callFrame.getState)
  println(interp.store.getState)
