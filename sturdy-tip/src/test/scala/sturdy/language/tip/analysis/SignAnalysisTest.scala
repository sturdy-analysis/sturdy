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
import sturdy.util.Labeled
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.language.tip.analysis.SignAnalysisSoundness.given

import java.nio.file.{Paths, Files, Path}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class SignAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip sign analysis"

  val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".tip")).sorted.foreach { p =>
    it must s"soundly analyze ${p.getFileName}" in {
      runSignAnalysis(p, 10)
    }
  }

  def runSignAnalysis(p: Path, steps: Int) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map(), steps)

      val onlyCalls = false
      val cfg = SignAnalysis.controlFlow(sensitive = true, onlyCalls, analysis)

      val aresult = analysis.failure.fallible(analysis.execute(program))

      val deadNodes = cfg.filterDeadNodes(SignAnalysis.allCfgNodes(program, onlyCalls))
      if (deadNodes.nonEmpty)
        println(s"Found dead code: $deadNodes")

      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      given CAllocationIntIncrement[AllocationSite] = interp.alloc
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      (aresult, analysis, cfg)
    } else {
      null
    }

object RunSignAnalysis extends App {
  val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip/record3.tip").toURI;
  val (res, analysis, cfg) = new SignAnalysisTest().runSignAnalysis(Paths.get(uri), 10)
  println(res)
  println(analysis.callFrame.getState)
  println(analysis.store.getState)
  println(analysis.print.getState)
//  println(cfg.toGraphViz)
}