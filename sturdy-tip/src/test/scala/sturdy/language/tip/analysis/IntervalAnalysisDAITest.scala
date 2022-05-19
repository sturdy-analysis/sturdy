package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.data.given
import sturdy.Soundness
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.failure.AFallible
import sturdy.effect.print.given
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.GenericInterpreter.{AllocationSite, FixIn}
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.effect.failure.{afallibleAbstractly, falliblePO}
import sturdy.fix.DAIFixpoint
import sturdy.util.Labeled
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.analysis.IntervalAnalysisSoundness.given
import sturdy.language.tip.analysis.IntervalAnalysis.{*, given}
import sturdy.language.tip.abstractions.isFunOrWhile

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class IntervalAnalysisDAITest extends AnyFlatSpec, Matchers:

  behavior of "Tip interval dai analysis"

  val uri = classOf[IntervalAnalysisTest].getResource("/sturdy/language/tip").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith("pushdown.tip")).sorted.foreach { p =>
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
      val comp: DaiTipOutCacheInterval = new DaiTipOutCacheInterval()
      val analysis = new IntervalAnalysis.Instance(Map(), Map()) {
        val fixpoint = new DAIFixpoint((dom: FixIn) => isFunOrWhile(dom))(comp)
      }

      //      val onlyCalls = false
      //      val cfg = IntervalAnalysis.controlFlow(sensitive = true, onlyCalls, analysis)

      val aresult = analysis.failure.fallible(analysis.execute(program))

      //      val deadNodes = cfg.filterDeadNodes(IntervalAnalysis.allCfgNodes(program, onlyCalls))
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

object RunIntervalDAIAnalysis extends App {
  val uri = classOf[IntervalAnalysisTest].getResource("/sturdy/language/tip/cfgloop.tip").toURI;
  val (res, analysis) = new IntervalAnalysisTest().runIntervalAnalysis(Paths.get(uri), 10)
  println(res)
  println(analysis.callFrame.getState)
  println(analysis.store.getState)
  println(analysis.print.getState)
  //  println(cfg.toGraphViz)
}
