package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.*
import sturdy.IsSound
import sturdy.data.given
import sturdy.Soundness
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.failure.{AFallible, given}
import sturdy.effect.print.given
import sturdy.effect.failure.{afallibleAbstractly, falliblePO}
import sturdy.effect.{TrySturdy, given}
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.fix.{StackConfig, State}
import sturdy.fix.summary.Summary.Result
import sturdy.fix.summary.{Summary, SummaryLogger}
import sturdy.incremental.{Identifiable, ListDelta}
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
import sturdy.fix.summary.Summary.given

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}
import scala.math.PartialOrdering

class IntervalIncrementalAnalysisTest extends AnyFlatSpec, should.Matchers:

  behavior of "Tip interval incremental analysis"

  val uri: URI = classOf[IntervalIncrementalAnalysisTest].getResource("/sturdy/language/tip").toURI
  val updateUri: URI = classOf[IntervalIncrementalAnalysisTest].getResource("/sturdy/language/tip/incremental.update").toURI

  Files.list(Paths.get(uri)).toScala(List).flatMap(p =>
    val update = Paths.get(updateUri.getPath, p.getFileName.toString)
    if(p.toString.contains("") && p.toString.endsWith(".tip") && Files.exists(update))
      Iterator((p,update))
    else
      Iterator()
  ).sorted.foreach { case (p: Path, update: Path) =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      runIntervalIncrementalAnalysis(p, update, StackConfig.StackedStates())
    }
    it must s"soundly analyze ${p.getFileName} with stacked frames" in {
      runIntervalIncrementalAnalysis(p, update, StackConfig.StackedCfgNodes())
    }
  }

  def runIntervalIncrementalAnalysis(initial: Path, update: Path, stackConfig: StackConfig): Unit =
    val initialProgram = parse(initial)
    val updatedProgram = parse(update)

    if (initialProgram.funs.exists(_.name == "main")) {
      println("INITIAL RUN")
      val initialRun = new IntervalAnalysis.InitialRunInstance(Map(), Map(), stackConfig, callSites = 0)
      initialRun.failure.fallible(initialRun.execute(initialProgram))


      println("\n\nINCREMENTAL UPDATE")
      val incrementalUpdate = new IntervalAnalysis.IncrementalUpdateInstance(initialRun)
      val changes = ListDelta.sub(initialProgram.funs.toList, updatedProgram.funs.toList)
      incrementalUpdate.failure.fallible(incrementalUpdate.execute(changes))

      println("\n\nFROM SCRATCH")
      val fromScratch = new IntervalAnalysis.InitialRunInstance(Map(), Map(), stackConfig, callSites = 0)
      fromScratch.failure.fallible(fromScratch.execute(Program(changes.keepOld.values.toSeq)))

      given widenVal: Widen[Value] = fromScratch.widenVal.force
      given widenIn: Widen[fromScratch.effectStack.In] = fromScratch.effectStack.widenIn(null)

      incrementalUpdate.summaryLogger should beSubsumedBy (fromScratch.summaryLogger)(using fromScratch.effectStack)

      // Test if the call graph of the incremental run is the same as the call graph of the from-scratch run.
      incrementalUpdate.callGraphLogger.calls.forEach ((caller, callees) =>
        fromScratch.callGraphLogger.calls.get(caller) match
          case null => fail(s"from-scratch run does not contain calls for $caller")
          case callees2 =>
            inside(s"calls of $caller") { _ =>
              callees shouldBe callees2
            }
      )

      incrementalUpdate.callGraphLogger.calledFrom.forEach((callee, callers) =>
        fromScratch.callGraphLogger.calledFrom.get(callee) match
          case null => fail(s"from-scratch run does not contain calls for $callee")
          case callers2 =>
            inside(s"callers of $callee") { _ =>
              callers shouldBe callers2
            }
      )
    }


  def beSubsumedBy[Dom, Codom, Callee](expected: SummaryLogger[Dom, Codom, Callee])
                                      (using state: State)
                                      (using Widen[Codom],
                                             Widen[state.In],
                                             Widen[state.Out]): Matcher[SummaryLogger[Dom, Codom, Callee]] =

    new Matcher[SummaryLogger[Dom, Codom, Callee]] {
      def apply(actual: SummaryLogger[Dom, Codom, Callee]): MatchResult =
        actual.cache.forEach((callee, actualSummary) =>
          expected.cache.get(callee) match
            case null => return MatchResult(false, s"expected summary logger does not contain summary for $callee", "")
            case expectedSummary =>
              val res = subsumedBy[state.In, (TrySturdy[Codom], state.Out)](actualSummary.asInstanceOf, expectedSummary.asInstanceOf)
              res match
                case MatchFailed(_) => return res
                case _ => {}
        )

        MatchResult(true,
          "actual summary logger is not subsumed by expected summary logger",
          "actual summary logger is subsumed by expected summary logger")
    }

  def subsumedBy[In: PartialOrder, Out: PartialOrder](actual: Summary[In,Out], expected: Summary[In,Out]): MatchResult =
    val pin: PartialOrder[In] = summon[PartialOrder[In]]
    val pres: PartialOrder[Result[In,Out]] = summon[PartialOrder[Result[In,Out]]]

    val inputNotSubsumed: mutable.ArrayBuffer[In] = mutable.ArrayBuffer.empty
    val outputNotSubsumed: mutable.ArrayBuffer[(Summary.Result[In,Out], Summary.Result[In,Out])] = mutable.ArrayBuffer.empty

    for(in1 <- actual.domain)
      val geq = expected.domain.filter(pin.lteq(in1,_))
      if(geq.isEmpty)
        inputNotSubsumed += in1

      for(in2 <- geq)
        if(! pres.lteq(actual(in1),expected(in2)))
          outputNotSubsumed += ((actual(in1), expected(in2)))

    MatchResult(matches = inputNotSubsumed.isEmpty && outputNotSubsumed.isEmpty,
                rawFailureMessage = s"summary\n$actual is not subsumed by summary\n$expected:\n" +
                                    s"inputs not subsumed $inputNotSubsumed\n" +
                                    s"outputs not subsumed $outputNotSubsumed",
                rawNegatedFailureMessage = s"summary $actual subsumed by $expected")


  def parse(p: Path): Program =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    Parser.parse(sourceCode)