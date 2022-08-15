package sturdy.language.tip.analysis

import apron.Polka
import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.data.given
import sturdy.Soundness
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.failure.{AFallible, afallibleAbstractly, falliblePO, given}
import sturdy.effect.print.given
import sturdy.fix.StackConfig
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.abstractions.isFunOrWhile
import sturdy.language.tip.*
import sturdy.util.{Labeled, LinearStateOperationCounter, Profiler}
import sturdy.values.booleans.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class RelationalAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip Relational analysis"

  val uri = classOf[RelationalAnalysisTest].getResource("/sturdy/language/tip").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    p.toString.contains("") && p.toString.endsWith(".tip")
  ).sorted.foreach { p =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      runRelationalAnalysis(p, StackConfig.StackedStates())
    }
    it must s"soundly analyze ${p.getFileName} with stacked frames" in {
      runRelationalAnalysis(p, StackConfig.StackedCfgNodes())
    }
  }

  def runRelationalAnalysis(p: Path, stackConfig: StackConfig) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    val polyManager = new Polka(false)

    if (program.funs.exists(_.name == "main")) {
      val analysis = new RelationalAnalysis.Instance(polyManager, Map(), Map(), stackConfig, 0)

      //      val onlyCalls = false
      //      val cfg = IntervalAnalysis.controlFlow(sensitive = true, onlyCalls, analysis)

      val aresult = analysis.failure.fallible(analysis.execute(program))
      Profiler.printLastMeasured()
      LinearStateOperationCounter.addToListAndReset()
      println(s"${LinearStateOperationCounter.toString} in the last tests")
      println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")
      //      val deadNodes = cfg.filterDeadNodes(IntervalAnalysis.allCfgNodes(program, onlyCalls))
      //      if (deadNodes.nonEmpty)
      //        println(s"Found dead code: $deadNodes")
    }
/*
      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      given CAllocationIntIncrement[AllocationSite] = interp.alloc
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      println(aresult)
      (aresult, analysis)
    } else {
      null
    }

*/