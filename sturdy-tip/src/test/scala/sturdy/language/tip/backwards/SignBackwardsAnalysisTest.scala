package sturdy.language.tip.backwards

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.Assertions.assertResult
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.{EffectStack, failure}
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.failure.{AFallible, CFallible, given}
import sturdy.effect.print.given
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.backward.SignBackwardsAnalysisSoundness.given
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
import sturdy.AbstractlySound
import sturdy.language.tip.backward.SignBackwardsAnalysis
import sturdy.language.tip.backward.SignBackwardsAnalysis.{*, given}

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

class SignBackwardsAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip sign backward analysis"

  val uri = classOf[SignBackwardsAnalysisTest].getResource("/MyTests").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    p.getFileName.toString != "simple4.tip"
  ).foreach { p =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      runSignAnalysis(p, StackConfig.StackedStates())
    }
  }


def runSignAnalysis(p: Path, stackConfig: StackConfig) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    val functions = program.funs.map(f => f.name -> f).toMap

    if (functions.contains("main")) {
      //println(s"Program is ${program}")
      val analysis = new SignBackwardsAnalysis.Instance(Map(), Map(), stackConfig)

      val inputs = () => ConcreteInterpreter.Value.IntValue(5)
      val interp = ConcreteInterpreter(Map(), Map(), inputs)
      val cresult = interp.failure.fallible(interp.execute(program))

      val res = cresult.get.asInt(using interp.failure)
      val expectedVal = analysis.intOps.toValue(List(res))

      println(s"Expected output of the program is ${cresult}")

      val aresult = analysis.failure.fallible(analysis.executeBack(program, expectedVal))

      type C = CFallible[ConcreteInterpreter.Value]
      type A = AFallible[(Seq[Value], Value)]

      given CAllocationIntIncrement[AllocationSite] = new CAllocationIntIncrement
      import sturdy.language.tip.backward.SignBackwardsAnalysisSoundness.{valuesAbstractly, addrAbstractly, po as poValue}

      val abstractlyValue: Abstractly[ConcreteInterpreter.Value, Value] = valuesAbstractly(using addrAbstractly)

      val numProgInputs = functions("main").params.size
      def abstractInputs(size: Int, input: () => ConcreteInterpreter.Value): Seq[Value] =
        0.until(size).map(_ => abstractlyValue(input()))

      val abstractly: Abstractly[C, A] = {
        case failure.CFallible.Unfailing(t) => AFallible.MaybeFailing((abstractInputs(numProgInputs, inputs), abstractlyValue(t)), Powerset())
        case failure.CFallible.Failing(kind, msg) => AFallible.Failing(Powerset((kind, msg)))
      }
      val poInputAndExpected: PartialOrder[(Seq[Value], Value)] = (x: (Seq[Value], Value), y: (Seq[Value], Value)) => (x, y) match
        case ((inputs1, expected1), (inputs2, expected2)) =>
          val inputOrder = inputs1.zip(inputs2).forall((i1, i2) => poValue.lteq(i1, i2))
          val expectedOrder = poValue.lteq(expected1, expected2)
          inputOrder && expectedOrder
      val po: PartialOrder[A] = (x: A, y: A) => (x, y) match
        case (AFallible.Diverging(_), _) => true
        case (AFallible.Unfailing(t1), AFallible.Unfailing(t2)) => poInputAndExpected.lteq(t1, t2)
        case (AFallible.Failing(fails1), AFallible.Failing(fails2)) => fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
        case (AFallible.Unfailing(t1), AFallible.MaybeFailing(t2, fails2)) => poInputAndExpected.lteq(t1, t2)
        case (AFallible.Failing(fails1), AFallible.MaybeFailing(t2, fails2)) => fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
        case (AFallible.MaybeFailing(t1, fails1), AFallible.MaybeFailing(t2, fails2)) => poInputAndExpected.lteq(t1, t2) && fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
        case _ => false
      val abstractlySound = AbstractlySound[C, A](using abstractly, po)

      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult)(using abstractlySound))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))

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

