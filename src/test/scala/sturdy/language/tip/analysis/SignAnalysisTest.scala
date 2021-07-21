package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser0 as P0, Parser as P}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.GenericInterpreter.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Program, Parser}
import sturdy.language.whilelang.ConcreteInterpreter.*
import sturdy.language.whilelang.ConcreteInterpreter.Value.*

import sturdy.effect.failure.given
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.language.tip.analysis.SignAnalysisSoundness.given

import java.nio.file.{Paths, Files}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class SignAnalysisTest extends AnyFlatSpec, Matchers:
  
  "TIP sign analysis" should "runs all example files" in {
    val steps = 1000

    val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip").toURI();
    val tipDir = Paths.get(uri)
    Files.list(tipDir).toScala(Iterator).filter(_.toString.endsWith(".tip")).foreach { p =>
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val program = Parser.parse(sourceCode)
      if (program.funs.exists(_.name == "main")) {
        println(s"Running ${p.getFileName}")

        val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
        val cresult = interp.captured(interp.execute(program))

        val analysis = SignAnalysis(Map(), Map(), steps)
        val aresult = analysis.captured(analysis.execute(program))

        given CAllocationIntIncrement[AllocationSite] = interp.effectOps
        assertResult(IsSound.Sound)(Soundness.isSound(cresult, aresult))
        assertResult(IsSound.Sound)(Soundness.isSound(interp, analysis))
      } else {
        println(s"${p.getFileName}: no main function")
      }
    }
  }
