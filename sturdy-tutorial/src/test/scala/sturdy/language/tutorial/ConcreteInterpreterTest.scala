package sturdy.language.tutorial

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFallible

import java.nio.file.{Path, Paths}
import scala.io.Source

/*
 * To test the correctness of the concrete interpreter we need to test that it is correct with respect to our
 * reference interpreter.
 */
class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "Concrete while interpreter"

  val add1 = Paths.get(classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tutorial/add1.while").toURI())
  val fac = Paths.get(classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tutorial/fac.while").toURI())

  testFile(add1, 1)
  testFile(add1, 5)
  testFile(fac, 5)

  def testFile(p: Path, arg: Int): Unit =
    it must s"correctly execute ${p.getFileName} with argument $arg"
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    val refInterp = new ReferenceInterpreter
    val concInterp = new ConcreteInterpreter
    val resRef = fallible(refInterp.runProg(arg, program))
    val resConc = fallible(concInterp.runProg(arg, program))
    assertResult(resRef)(resConc)

  def fallible[A](f: => A): CFallible[A] =
    try {
      val res = f
      CFallible.Unfailing(res)
    } catch {
      case CFailureException(kind, msg) => CFallible.Failing(kind, msg)
      case ex => throw ex
    }