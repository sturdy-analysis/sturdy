package sturdy.language.tutorial

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Path, Paths}
import scala.io.Source
import sturdy.effect.failure.AFallible

class SignInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "Sign Interpreter"

  val add1: Path = Paths.get(classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tutorial/add1.while").toURI)
  val fac: Path = Paths.get(classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tutorial/fac.while").toURI)

  testFile(add1, Sign.Zero, Sign.Pos)
  testFile(add1, Sign.Neg, Sign.Top)
  testFile(fac, Sign.Zero, Sign.Pos)

  def testFile(p: Path, arg: Sign, expected: Sign): Unit =
    it must s"execute ${p.getFileName} with argument $arg returning $expected" in {
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val program = Parser.parse(sourceCode)
      val interp = new SignInterpreter
      val res = interp.failure.fallible(
        interp.runProg(arg, program)
      )
      res match
        case AFallible.Unfailing(v) => assertResult(expected)(v)
        case AFallible.MaybeFailing(v,_) => assertResult(expected)(v)
        case AFallible.Failing(fails) => assert(false, s"Expected $expected but execution failed: $fails")
    }