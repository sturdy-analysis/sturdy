package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.effect.print.{APrintPrefix, Print, given}
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.GenericInterpreter.{AllocationSite, FixIn}
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.effect.failure.given
import sturdy.fix.{DAIFixpoint, Fixpoint}
import sturdy.language.tip.GenericInterpreter
import sturdy.util.{Labled, Profiler}
import sturdy.{*, given}
import sturdy.data.given
import sturdy.effect.store.{AStoreMultiAddrThreadded, Store}
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

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class SignAnalysisDAITest extends AnyFlatSpec, Matchers:

  behavior of "Tip sign dai analysis"

  val uri = classOf[SignAnalysisDAITest].getResource("/sturdy/language/tip").toURI

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
    Profiler.start("init")
    val program = Parser.parse(sourceCode)
    Profiler.end("init")
    Profiler.printByName("init")
    Profiler.reset()
    if (program.funs.exists(_.name == "main")) {
      val comp: DaiTipOutCache = new DaiTipOutCache()
      val analysis = new SignAnalysis.Instance(Map(), Map()) {
        override val fixpoint = new DAIFixpoint((dom: FixIn) => isFunOrWhile(dom))(comp)
      }
      
      val aresult = analysis.failure.fallible(analysis.execute(program))
//      println(comp.outCache)
      Profiler.printLastMeasured()
      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      given CAllocationIntIncrement[AllocationSite] = interp.alloc
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      (aresult, analysis)
    } else {
      null
    }

//object RunSignAnalysisDAI extends App {
//  val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip/7x7.tip").toURI;
//  val (res, analysis) = new SignAnalysisDAITest().runSignAnalysis(Paths.get(uri))
//  println(res)
//  println(analysis.callFrame.getState)
//  println(analysis.store.getState)
//  println(analysis.print.getState)
////  println(cfg.toGraphViz)
//}