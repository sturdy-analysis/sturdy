package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.data.given
import sturdy.Soundness
import sturdy.control.{ControlEvent, ControlEventGraphBuilder, ControlGraph, ControlTree, FixpointControlEvent, PrintingControlObserver, RecordingControlObserver}
import sturdy.effect.allocation.CAllocatorIntIncrement
import sturdy.effect.failure.{AFallible, given}
import sturdy.effect.print.given
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.effect.failure.{afallibleAbstractly, falliblePO}
import sturdy.fix.StackConfig
import sturdy.fix.iter.Config.{Innermost, Outermost, Topmost}
import sturdy.util.{Labeled, LinearStateOperationCounter, Profiler}
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.analysis.IntervalAnalysisSoundness.given
import sturdy.language.tip.analysis.IntervalAnalysis.{*, given}
import sturdy.language.tip.abstractions.isFunOrWhile

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class IntervalAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip interval analysis"

  val uri: URI = classOf[IntervalAnalysisTest].getResource("/sturdy/language/tip").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    p.toString.contains("") && p.toString.endsWith(".tip")
  ).sorted.foreach { p =>
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    for (iter <- fix.iter.Config.values) {
      val results: ListBuffer[AFallible[Value]] = ListBuffer.empty

      it must s"soundly analyze ${p.getFileName} with stacked states (storeIntermediateOutput = false), $iter" in {
        results += runIntervalAnalysis(p, program, StackConfig.StackedStates(storeIntermediateOutput = false, storeNonrecursiveOutput = false), iter)._1 
      }
      it must s"soundly analyze ${p.getFileName} with stacked states (storeNonrecursiveOutput = true), $iter" in {
        results += runIntervalAnalysis(p, program, StackConfig.StackedStates(storeIntermediateOutput = false, storeNonrecursiveOutput = true), iter)._1
      }
      it must s"soundly analyze ${p.getFileName} with stacked states (storeIntermediateOutput = true), $iter" in {
        results += runIntervalAnalysis(p, program, StackConfig.StackedStates(storeIntermediateOutput = true, storeNonrecursiveOutput = false), iter)._1
      }
      it must s"soundly analyze ${p.getFileName} with stacked states (storeIntermediateOutput = true, storeNonrecursiveOutput = true), $iter" in {
        results += runIntervalAnalysis(p, program, StackConfig.StackedStates(storeIntermediateOutput = true, storeNonrecursiveOutput = true), iter)._1
      }
      it must s"soundly analyze ${p.getFileName} with stacked states minimal, $iter" in {
        results += runIntervalAnalysis(p, program, StackConfig.StackedStatesMinimal, iter)._1
      }
      it must s"Compare analysis results ${p.getFileName}, $iter" in {
        if (results.size > 1) {
          val expected = results.head
          results.tail.foreach { res =>
            assertResult(expected)(res)
          }
        }
      }
    }
  }

  def runIntervalAnalysis(p: Path, program: Program, stackConfig: StackConfig, iterConfig: fix.iter.Config): (AFallible[Value], Instance) =

    if (program.funs.exists(_.name == "main")) {
      Profiler.reset()
      val analysis = new IntervalAnalysis.Instance(Map(), Map(), stackConfig, 0, iterConfig)
//      val rec = analysis.addControlObserver(new RecordingControlObserver)
//      analysis.addControlObserver(new PrintingControlObserver()(println))
//      val graphBuilder = analysis.addControlObserver(new ControlEventGraphBuilder)

//      val onlyCalls = false
//      val cfg = IntervalAnalysis.controlFlow(sensitive = true, onlyCalls, analysis)

      val aresult = analysis.failure.fallible(analysis.execute(program))
      Profiler.printLastMeasured()
//      LinearStateOperationCounter.addToListAndReset()
//      println(s"${LinearStateOperationCounter.toString} in the last tests")
//      println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")

//      println(graphBuilder.get.toGraphViz)

      val interp = ConcreteInterpreter(() => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      given CAllocatorIntIncrement[AllocationSite] = interp.alloc
      println(cresult)
      println(aresult)
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      println(aresult)
//      println(rec)
      (aresult, analysis)
    } else {
      (null, null)
    }

