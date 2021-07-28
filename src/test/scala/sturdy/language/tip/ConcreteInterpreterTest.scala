package sturdy.language.tip

import cats.parse.{Numbers, Parser0 as P0, Parser as P}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.whilelang.ConcreteInterpreter.*
import sturdy.language.whilelang.ConcreteInterpreter.Value.*

import java.nio.file.Path
import java.nio.file.{Paths, Files}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:

  def runFile(p: Path): Unit =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      print(s"${p.getFileName}")
      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      Try(interp.execute(program)) match
        case Success(_) => println(" prints: " + interp.effectOps.getPrinted)
        case Failure(e) => println(" errors: " + e)
    } else {
      println(s"${p.getFileName}: no main function")
    }

  "TIP interpreter" should "run all example files" in {
    val uri = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tip").toURI();
    val tipDir = Paths.get(uri)
    Files.list(tipDir).toScala(List).sorted.filter(_.toString.endsWith(".tip")).foreach { p =>
      runFile(p)
    }
  }
