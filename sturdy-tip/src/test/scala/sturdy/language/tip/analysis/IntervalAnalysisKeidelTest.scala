package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser0 as P0, Parser as P}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.data.given
import sturdy.Soundness
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.failure.AFallible
import sturdy.effect.print.{APrintPrefix, given}
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.GenericInterpreter.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Program, Parser}
import sturdy.effect.failure.{falliblePO, afallibleAbstractly}
import sturdy.fix.iter.Config
import sturdy.fix.{DAIFixpoint, KeidelFixpoint}
import sturdy.util.Labled
import sturdy.{*, given}
import sturdy.data.{*, given}
import sturdy.fix.InsensitiveStack
import sturdy.language.tip.GenericInterpreter.FixIn
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

import java.nio.file.{Path, Paths, Files}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class IntervalAnalysisKeidelTest extends AnyFlatSpec, Matchers:

  behavior of "Tip interval Keidel analysis"

  val uri = classOf[IntervalAnalysisTest].getResource("/sturdy/language/tip").toURI;

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
      val analysis = new IntervalAnalysis.Instance(Map(), Map()) {
        val fixpoint = new KeidelFixpoint(
          isFunOrWhile, Seq(Config.Innermost, Config.Innermost), new InsensitiveStack[FixIn, InState]())
      }

      val aresult = analysis.failure.fallible(analysis.execute(program))
//        println(StackManager.keidelFixpoint.asInstanceOf[KeidelFixpoint[GenericInterpreter.FixIn,
//          GenericInterpreter.FixOut[SignAnalysis.Value],
//          Map[AllocationSiteAddr, SignAnalysis.Value],
//          (Map[AllocationSiteAddr, SignAnalysis.Value], APrintPrefix.PrintResult[SignAnalysis.Value]),
//          (Map[AllocationSiteAddr, SignAnalysis.Value], APrintPrefix.PrintResult[SignAnalysis.Value])]].results)
      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      given CAllocationIntIncrement[AllocationSite] = interp.alloc
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      (aresult, analysis)
    } else {
      null
    }
