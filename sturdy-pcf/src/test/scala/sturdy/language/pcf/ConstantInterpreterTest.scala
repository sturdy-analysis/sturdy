package sturdy.language.pcf

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.{IsSound, Soundness}
import sturdy.language.pcf.Parser
import sturdy.values.{Abstractly, Topped}

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}
import sturdy.effect.failure.soundnessAFallible
import sturdy.AbstractlySound
import sturdy.values.given
import sturdy.values.integer.given
import ConstantInterpreter.given
import sturdy.fix.Fixpoint

class ConstantInterpreterTest extends AnyFlatSpec, Matchers:

  behavior of "Tip constant interpreter"

  val uri = classOf[ConstantInterpreterTest].getResource("/sturdy/language/pcf").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".pcf")).sorted.foreach { p =>
    it must s"execute ${p.getFileName}" in {
      runFile(p)
    }
  }

  Fixpoint.DEBUG = true
  
  def runFile(p: Path): Unit =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    if (program.definitions.contains("main")) {
      val interp = new ConcreteInterpreter.Instance(() => ConcreteInterpreter.Value.Int(5))
      val result = interp.failure.fallible(interp.evalProgram(program))

      val ainterp = new ConstantInterpreter.Instance(() => ConstantInterpreter.Value.Int(Topped.Top))
      val aresult = ainterp.failure.fallible(ainterp.evalProgram(program))

      summon[Abstractly[ConcreteInterpreter.Value, ConstantInterpreter.Value]]
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(result, aresult))
//      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, ainterp))

      println(aresult)
    }