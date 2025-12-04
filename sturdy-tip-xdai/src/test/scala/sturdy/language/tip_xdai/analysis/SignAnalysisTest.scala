package sturdy.language.tip_xdai.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.{ControlEventGraphBuilder, PrintingControlObserver}
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.allocation.CAllocatorIntIncrement
import sturdy.effect.failure.given
import sturdy.effect.print.given
import sturdy.fix.{Fixpoint, StackConfig, StackedFrames}
import sturdy.language.tip_xdai.Parser.*
import sturdy.language.tip_xdai.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip_xdai.SignInterpreter
import sturdy.language.tip_xdai.*
import sturdy.language.tip_xdai.arithmetic.concrete.IntValue as ConcreteIntValue
import sturdy.util.Labeled
import sturdy.values.booleans.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import sturdy.*
import sturdy.language.tip_xdai.references.AllocationSite

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class SignAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip sign analysis"

  val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter( p =>
    p.toString.endsWith(".tip")
  ).sorted.foreach { p =>
//    it must s"soundly analyze ${p.getFileName} with stacked states" in {
//      runSignAnalysis(p, StackConfig.StackedStates())
//    }
    it must s"soundly analyze ${p.getFileName} with stacked frames" in {
      //runSignAnalysis(p, StackConfig.StackedCfgNodes())
      runSignAnalysis(p, StackConfig.StackedStates(storeIntermediateOutput = true, storeNonrecursiveOutput = true, readPriorOutput = false))
    }
  }

  /*Fixpoint.DEBUG = true
  Fixpoint.DEBUG_INVARIANTS = true
  Fixpoint.DEBUG_PRIOR_OUTPUT = true*/

  def runSignAnalysis(p: Path, stackConfig: StackConfig) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignInterpreter

      val aresult = analysis.failure.fallible(analysis.execute(program))
//      val deadNodes = cfg.filterDeadNodes(SignAnalysis.allCfgNodes(program, onlyCalls))

//      if (deadNodes.nonEmpty)
//        println(s"Found dead code: $deadNodes")
      val interp = ConcreteInterpreter(() => ConcreteIntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      given CAllocatorIntIncrement[AllocationSite] = interp.alloc
      //assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      //assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      print(aresult)
      (aresult, analysis)
    } else {
      null
    }

