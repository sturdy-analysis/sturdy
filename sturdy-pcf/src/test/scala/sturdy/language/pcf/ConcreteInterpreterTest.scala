package sturdy.language.pcf

import cats.parse.{Numbers, Parser0 as P0, Parser as P}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.pcf.Parser

import java.nio.file.Path
import java.nio.file.{Paths, Files}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:

  behavior of "Tip concrete interpreter"

  val uri = classOf[ConcreteInterpreterTest].getResource("/sturdy/language/pcf").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".pcf")).sorted.foreach { p =>
    it must s"execute ${p.getFileName}" in {
      runFile(p)
    }
  }

  def runFile(p: Path): Unit =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    if (program.definitions.contains("main")) {
      val interp = new ConcreteInterpreter.Instance(() => ConcreteInterpreter.Value.Int(5))
      val result = interp.failure.fallible(interp.evalProgram(program))
      println(result)
    }