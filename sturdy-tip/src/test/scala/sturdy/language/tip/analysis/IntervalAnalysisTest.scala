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

import sturdy.effect.failure.given
import sturdy.util.Labled
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.language.tip.analysis.IntervalAnalysisSoundness.given

import java.nio.file.{Paths, Files, Path}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class IntervalAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip interval analysis"

  val uri = classOf[IntervalAnalysisTest].getResource("/sturdy/language/tip").toURI();
  
  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".tip")).sorted.foreach { p =>
    it must s"soundly analyze ${p.getFileName}" in {
      runIntervalAnalysis(p, 10)
    }
  }

  def runIntervalAnalysis(p: Path, steps: Int) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      val analysis = IntervalAnalysis(Map(), Map(), steps)

      val onlyCalls = false
      val cfg = IntervalAnalysis.controlFlow(sensitive = true, onlyCalls, analysis)

      val aresult = analysis.effects.fallible(analysis.execute(program))

      val deadNodes = cfg.filterDeadNodes(IntervalAnalysis.allCfgNodes(program, onlyCalls))
      if (deadNodes.nonEmpty)
        println(s"Found dead code: $deadNodes")

      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.effects.fallible(interp.execute(program))
      given CAllocationIntIncrement[AllocationSite] = interp.effects
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      (aresult, analysis, cfg)
    } else {
      null
    }

object RunIntervalAnalysis extends App {
  val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip/cfgloop.tip").toURI();
  val (res, analysis, cfg) = new IntervalAnalysisTest().runIntervalAnalysis(Paths.get(uri), 10)
  println(res)
  println(analysis.effects.getCallFrame)
  println(analysis.effects.getStore)
  println(analysis.effects.getPrinted)
//  println(cfg.toGraphViz)
}
