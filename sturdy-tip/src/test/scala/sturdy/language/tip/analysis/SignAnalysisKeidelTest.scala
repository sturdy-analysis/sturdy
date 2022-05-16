package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser0 as P0, Parser as P}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.{EffectStack, AnalysisState, TrySturdy, RecurrentCall}
import sturdy.effect.print.{Print, APrintPrefix, given}
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.GenericInterpreter.{FixIn, finiteFixIn, AllocationSite}
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Program, Parser}
import sturdy.effect.failure.given
import sturdy.fix.{DAIFixpoint, KeidelFixpoint, Fixpoint}
import sturdy.fix.KeidelFixpoint.given
import sturdy.language.tip.GenericInterpreter
import sturdy.util.Labled
import sturdy.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.store.{AStoreMultiAddrThreadded, Store}
import sturdy.fix.FiniteStack
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
import sturdy.language.tip.abstractions.isFunOrWhile
import sturdy.fix.iter.Config
import sturdy.util.Lazy

import java.nio.file.{Path, Paths, Files}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class SignAnalysisKeidelTest extends AnyFlatSpec, Matchers:

  behavior of "Tip sign Keidel analysis"

  val uri = classOf[SignAnalysisKeidelTest].getResource("/sturdy/language/tip").toURI

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    //    !p.toString.contains("record") &&
    //    p.toString.contains("fib") &&
    p.toString.endsWith(".tip")
  ).sorted.foreach { p =>

    it must s"soundly analyze ${p.getFileName}" in {
      runSignAnalysis(p)
    }

  }

  def runSignAnalysis(p: Path) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {

      val analysis = new SignAnalysis.KeidelInstance(Map(), Map())
      val aresult = analysis.failure.fallible(analysis.execute(program))
//      println(StackManager.keidelFixpoint.asInstanceOf[KeidelFixpoint[GenericInterpreter.FixIn,
//        GenericInterpreter.FixOut[SignAnalysis.Value],
//        Map[AllocationSiteAddr, SignAnalysis.Value],
//        (Map[AllocationSiteAddr, SignAnalysis.Value], APrintPrefix.PrintResult[SignAnalysis.Value]),
//        (Map[AllocationSiteAddr, SignAnalysis.Value], APrintPrefix.PrintResult[SignAnalysis.Value])]].results)

      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      given CAllocationIntIncrement[AllocationSite] = interp.alloc
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      (aresult, analysis)
    } else {
      null
    }

object RunSignAnalysisKeidel extends App {
  val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip/7x7.tip").toURI;
  val (res, analysis) = new SignAnalysisKeidelTest().runSignAnalysis(Paths.get(uri))
  println(res)
  println(analysis.callFrame.getState)
  println(analysis.store.getState)
  println(analysis.print.getState)
  //  println(cfg.toGraphViz)
}