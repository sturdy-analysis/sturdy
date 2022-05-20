package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.effect.print.given
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.GenericInterpreter.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.effect.failure.given
import sturdy.effect.print.APrintPrefix
import sturdy.fix.{Fixpoint, Stack}
import sturdy.language.tip.GenericInterpreter
import sturdy.util.Labeled
import sturdy.{*, given}
import sturdy.data.{*, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.analysis.SignAnalysisSoundness.given
import sturdy.language.tip.analysis.SignAnalysis.{*, given}

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class SignAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip sign analysis"

  val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter( p =>
    !p.toString.endsWith("00Stack.tip") && !p.toString.endsWith("Ten.tip") && !p.toString.endsWith("00.tip") && p.toString.endsWith(".tip")
  ).sorted.foreach { p =>
    it must s"soundly analyze ${p.getFileName}" in {
      runSignAnalysis(p, 0)
    }
  }

  def runSignAnalysis(p: Path, steps: Int) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map())

//      val onlyCalls = false
//      val cfg = SignAnalysis.controlFlow(sensitive = true, onlyCalls, analysis)

      val aresult = analysis.failure.fallible(analysis.execute(program))
//      val deadNodes = cfg.filterDeadNodes(SignAnalysis.allCfgNodes(program, onlyCalls))

//      if (deadNodes.nonEmpty)
//        println(s"Found dead code: $deadNodes")
      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      given CAllocationIntIncrement[AllocationSite] = interp.alloc
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      (aresult, analysis)
    } else {
      null
    }

object RunSignAnalysis extends App {
  val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip/record3.tip").toURI;
  val (res, analysis) = new SignAnalysisTest().runSignAnalysis(Paths.get(uri), 10)
  println(res)
  println(analysis.callFrame.getState)
  println(analysis.store.getState)
  println(analysis.print.getState)
//  println(cfg.toGraphViz)
}