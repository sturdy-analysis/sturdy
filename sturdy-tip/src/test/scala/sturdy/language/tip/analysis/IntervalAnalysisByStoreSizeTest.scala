/*
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
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.effect.failure.{afallibleAbstractly, falliblePO}
import sturdy.fix.{Fixpoint, StackConfig}
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
import sturdy.language.tip.analysis.IntervalAnalysisSoundness.given
import sturdy.language.tip.analysis.IntervalAnalysis.{*, given}
import sturdy.language.tip.abstractions.isFunOrWhile

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}


class IntervalAnalysisByStoreSizeTest extends AnyFlatSpec, Matchers:

  behavior of "Tip interval analysis"

  var uri = classOf[IntervalAnalysisByStoreSizeTest].getResource("/sturdy/language/tip").toURI


  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(s"00100_20000Stack.tip")).sorted.foreach { p =>
    it must s"warm up " in {
      runIntervalAnalysis(p, StackConfig.StackedCfgNodes())
      runIntervalAnalysis(p, StackConfig.StackedStates())
    }
  }

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith("00100_20000Stack.tip")).sorted.foreach { p =>
    it must s"soundly analyze ${p.getFileName}" in {
      runIntervalAnalysis(p, StackConfig.StackedCfgNodes())
      runIntervalAnalysis(p, StackConfig.StackedStates())
    }
  }
//
//  for (storeSize <- List(10, 100, 1000, 10000, 100000, 200000, 300000, 400000, 500000)) {
//    Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(s"factorialManyVars$storeSize.tip")).sorted.foreach { p =>
//      it must s"soundly analyze ${p.getFileName} with store size $storeSize" in {
//        runIntervalAnalysis(p)
//      }
//    }
//  }

  def runIntervalAnalysis(p: Path, stackConfig: StackConfig) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      val analysis = new IntervalAnalysis.Instance(Map(), Map(), stackConfig, 0)

      var aresult: AFallible[Value] = AFallible.Unfailing(Value.TopValue)
//      while (true)
        aresult = Profiler.addTime("analysis"){analysis.failure.fallible(analysis.execute(program))}

//      println(aresult)
//      Profiler.saveTimesAndReset()
//      Profiler.printSavedTimes()
//      LinearStateOperationCounter.addToListAndReset()
//      println(s"${LinearStateOperationCounter.toString} in the last tests")
//      println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")
//      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
//      val cresult = interp.failure.fallible(interp.execute(program))
//      given CAllocationIntIncrement[AllocationSite] = interp.alloc
//      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
//      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))

      LinearStateOperationCounter.clearAll()
      (aresult, analysis)
    } else {
      null
    }
*/