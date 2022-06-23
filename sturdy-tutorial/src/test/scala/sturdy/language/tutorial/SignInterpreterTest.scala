package sturdy.language.tutorial

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.{Soundness, IsSound, AbstractlySound, given}

import java.nio.file.{Path, Paths}
import scala.io.Source
import sturdy.effect.failure.{AFallible, given}

/*
 * To test the soundness of our sign analysis we need to test if the sign interpreter correctly approximates the
 * concrete interpreter.
 */
class SignInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "Sign Interpreter"

  val add1: Path = Paths.get(classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tutorial/add1.while").toURI)
  val fac: Path = Paths.get(classOf[ConcreteInterpreterTest].getResource("/sturdy/language/tutorial/fac.while").toURI)

  testFile(add1, Sign.Zero, Sign.Pos)
  testFile(add1, Sign.Neg, Sign.TopSign)
  testFile(fac, Sign.Zero, Sign.Pos)

  testSoundness(add1, 0)
  testSoundness(add1, 1)
  testSoundness(add1, -1)

  testSoundness(fac, 0)
  testSoundness(fac, 5)

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
        case AFallible.Diverging(recur) => assert(false, s"Expected $expected but execution diverged: $recur")
    }

  def testSoundness(p: Path, arg: Int): Unit =
    it must s"execute ${p.getFileName} with alpha($arg) correctly approximating the concrete interpreter" in {
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val program = Parser.parse(sourceCode)
      val cInterp = new ConcreteInterpreter
      val sInterp = new SignInterpreter
      val cRes = fallible(cInterp.runProg(arg, program))
      val sRes = sInterp.failure.fallible(sInterp.runProg(valuesAbstractly.apply(arg), program))
      // result needs to be sound
      assertResult(IsSound.Sound)(Soundness.isSound(cRes,sRes))
      // interpreter state needs to be sound
      assertResult(IsSound.Sound)(Soundness.isSound(cInterp, sInterp))
    }