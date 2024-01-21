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
import sturdy.language.tip.analysis.SignAnalysis.{*, given}
import sturdy.language.tip.analysis.SignAnalysisSoundness.given
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
import sturdy.language.tip.backward.SignBackwardsAnalysis

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class SignBackwardsAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip sign backward analysis"

  val uri = classOf[SignBackwardsAnalysisTest].getResource("/MyTests").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    p.getFileName.toString == "simple4.tip"
  ).foreach { p =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      runSignAnalysis(p, StackConfig.StackedStates())
    }
  }

//  Files.list(Paths.get(uri)).toScala(List).filter(p =>
//    !p.toString.endsWith("00Stack.tip") && !p.toString.endsWith("Ten.tip") && !p.toString.endsWith("00.tip") && p.toString.endsWith(".tip")
//  ).sorted.foreach { p =>
//    it must s"soundly analyze ${p.getFileName} with stacked states" in {
//      runSignAnalysis(p, StackConfig.StackedStates())
//    }
//  }


def runSignAnalysis(p: Path, stackConfig: StackConfig) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignBackwardsAnalysis.Instance(Map(), Map(), stackConfig)
      val expectedVal = analysis.intOps.toValue(List(5))

      val aresult = analysis.failure.fallible(analysis.executeBack(program, expectedVal))


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

