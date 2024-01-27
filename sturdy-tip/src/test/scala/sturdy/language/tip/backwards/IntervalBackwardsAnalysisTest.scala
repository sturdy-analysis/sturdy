package sturdy.language.tip.backwards

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.failure.{AFallible, given}
import sturdy.effect.print.given
import sturdy.fix.{Fixpoint, StackConfig, StackedFrames}
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
//import sturdy.language.tip.analysis.SignAnalysis.{*, given}
//import sturdy.language.tip.analysis.SignAnalysisSoundness.given
import sturdy.language.tip.*
import sturdy.util.Labeled
import sturdy.values.booleans.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{*, given}
import sturdy.*
import sturdy.language.tip.backward.{IntervalBackwardAnalysis}


import sturdy.language.tip.backward.BackwardsInterpreter


import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class IntervalBackwardsAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip sign backward analysis"

  val uri = classOf[IntervalBackwardsAnalysisTest].getResource("/MyTests").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    p.getFileName.toString == "simple6.tip"
  ).foreach { p =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      runIntervalAnalysis(p, StackConfig.StackedStates())
    }
  }

  //  Files.list(Paths.get(uri)).toScala(List).filter( p =>
  //    !p.toString.endsWith("00Stack.tip") && !p.toString.endsWith("Ten.tip") && !p.toString.endsWith("00.tip") && p.toString.endsWith("main.tip")
  //  ).sorted.foreach { p =>
  //    it must s"soundly analyze ${p.getFileName} with stacked states" in {
  //      runSignAnalysis(p, StackConfig.StackedStates())
  //    }
  //  }

  def runIntervalAnalysis(p: Path, stackConfig: StackConfig) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      println(s"$program")
      val analysis = new IntervalBackwardAnalysis.Instance(Map(), Map(), stackConfig)

      //val intervalValue = Interval.I(2, 10)

      //val resultVal = analysis.topValue
      val resultVal = analysis.intOps.toValue(List(2,5))
      val aresult = analysis.failure.fallible(analysis.executeBack(program, resultVal))

      println(s"Backward run of ${p.getFileName} ")
      aresult match
        case AFallible.Unfailing(t) => println(s"  yields ${t._2} given arguments ${t._1}")
        case AFallible.Failing(msgs) => println(s"  fails $msgs")
        case AFallible.MaybeFailing(t, msgs) => println(s"  may yield ${t._2} given arguments ${t._1}, or fails $msgs")
        case AFallible.Diverging(recur) => println(s"  diverges ($recur)")

      (aresult, analysis)
    } else {
      null
    }

