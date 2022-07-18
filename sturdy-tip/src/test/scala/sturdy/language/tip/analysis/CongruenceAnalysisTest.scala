package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.data.given
import sturdy.Soundness
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.failure.{AFallible, given}
import sturdy.effect.print.given
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.effect.failure.{afallibleAbstractly, falliblePO}
import sturdy.fix.StackConfig
import sturdy.util.{Labeled, LinearStateOperationCounter, Profiler}
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.analysis.CongruenceAnalysisSoundness.given
import sturdy.language.tip.analysis.CongruenceAnalysis.{*, given}
import sturdy.language.tip.abstractions.isFunOrWhile

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class CongruenceAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip Congruence analysis"

  val uri = classOf[CongruenceAnalysisTest].getResource("/sturdy/language/tip").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter( p =>
    !p.toString.endsWith("00Stack.tip") && !p.toString.endsWith("Ten.tip") && !p.toString.endsWith("00.tip") && p.toString.endsWith(".tip")
  ).sorted.foreach { p =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      runCongruenceAnalysis(p, StackConfig.StackedStates())
    }
    it must s"soundly analyze ${p.getFileName} with stacked frames" in {
      runCongruenceAnalysis(p, StackConfig.StackedCfgNodes())
    }
  }

  def runCongruenceAnalysis(p: Path, stackConfig: StackConfig) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val analysis = new CongruenceAnalysis.Instance(Map(), Map(), stackConfig)

      val aresult = analysis.failure.fallible(analysis.execute(program))

      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      given CAllocationIntIncrement[AllocationSite] = interp.alloc
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      (aresult, analysis)
    } else {
      null
    }