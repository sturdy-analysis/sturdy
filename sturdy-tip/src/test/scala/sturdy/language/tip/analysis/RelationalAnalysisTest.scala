package sturdy.language.tip.analysis

import apron.{Polka, Texpr1Node}
import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.{*, given}
import sturdy.data.given
import sturdy.Soundness
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.failure.{AFallible, AssertionFailure, afallibleAbstractly, falliblePO, given}
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
  val recursiveProgram: Array[String] = Source.fromFile(classOf[RelationalAnalysisTest].getResource("/sturdy/language/recursive_programs").toURI).getLines.toArray

  // Crash the analysis since Interpreter change
  val excluded : Array[String] = Array("factorial_iterative.tip",
    "fib.tip",
    "interval0.tip",
    "interval1.tip",
    "interval3.tip",
    "late_effect.tip",
    "loop_nested.tip",
    "shape.tip",
    "slicing.tip", // too long because no overflow handling
    "while_short_if.tip",
    "verybusy.tip")

  // different environments error, when applying constraints
  val diffEnv : Array[String] = Array("equal.tip",
    "large3_2Stack.tip",
    "liveness.tip",
    "ptr6.tip",
    "reaching.tip",
    "verybusy.tip",
    "code.tip")

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
      p.toString.endsWith(".tip") && p.toString.contains("rel_id")
  ).sorted.foreach { p =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      runRelationalAnalysis(p, StackConfig.StackedStates())
    }
//    it must s"soundly analyze ${p.getFileName} with stacked frames" in {
//      runRelationalAnalysis(p, StackConfig.StackedCfgNodes())
//    }
  }

  def runRelationalAnalysis(p: Path, stackConfig: StackConfig) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    val polyManager = new Polka(false)

    if (program.funs.exists(_.name == "main")) {
      val analysis = new RelationalAnalysis.Instance(polyManager, stackConfig, 0)

      val aresult = analysis.failure.fallible(analysis.execute(program))
      val interp = ConcreteInterpreter(() => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))

      given CAllocationIntIncrement[AllocationSite] = interp.alloc

//      println(s"CONCRETE : $cresult")
//      println(s"ABSTRACT : $aresult")

      // compute number of assertions in program
      val unprovedAsserts = aresult.failures.set.filter(_._1.isInstanceOf[AssertionFailure[_]])
      val unprovedAssertsCount = unprovedAsserts.size

      // subtract number of maybefailing assertions
      // println("#assertions = " + program.assertCount + "; #unproved = " + unprovedAsserts)
      val allAsserts = program.assertCount
      val successfulAsserts = program.assertCount - unprovedAssertsCount
      val successfulFraction = if (allAsserts == 0) 1.0 else successfulAsserts.toDouble / allAsserts
      val failingFraction = if (allAsserts == 0) 0.0 else unprovedAssertsCount.toDouble / allAsserts
      if (unprovedAssertsCount == 0)
        println("All assertions proved or unreachable")
      else
        println(s"Failing assertions found in ${p.getFileName}: $unprovedAsserts out of $allAsserts failed (${failingFraction * 100}% failed)")
      assert(unprovedAsserts.isEmpty, s", ${unprovedAsserts.size} assertion(s) have failed")

      val soundness = new RelationalAnalysisSoundness(analysis.apron)
      import soundness.given
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      (aresult, analysis)
    } else {
      null
    }

