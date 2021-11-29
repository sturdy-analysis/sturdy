package sturdy.language.tutorial

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Path, Paths}
import scala.io.Source
import sturdy.language.tutorial.Parser

class ReferenceInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "While reference interpreter"

  val add1 = Paths.get(classOf[ReferenceInterpreterTest].getResource("/sturdy/language/tutorial/add1.while").toURI())
  val fac = Paths.get(classOf[ReferenceInterpreterTest].getResource("/sturdy/language/tutorial/fac.while").toURI())

  it must s"execute ${add1.getFileName}" in {
    assertResult(2)(runFile(add1,1))
  }

  it must s"execute ${fac.getFileName}" in {
    assertResult(120)(runFile(fac,5))
  }

  def runFile(p: Path, arg: Int): Int =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    val interp = new ReferenceInterpreter
    interp.runProg(arg, program)