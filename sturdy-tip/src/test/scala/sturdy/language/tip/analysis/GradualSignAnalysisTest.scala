package sturdy.language.tip.analysis


import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.EffectStack
import sturdy.effect.print.given
import sturdy.effect.allocation.CAllocatorIntIncrement
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.effect.failure.given
import sturdy.fix.{Fixpoint, StackConfig, StackedFrames}
import sturdy.language.tip.GenericInterpreter
import sturdy.util.Labeled
import sturdy.{*, given}
import sturdy.data.{*, given}
import sturdy.fix.StackConfig.StackedStates
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.analysis.SignAnalysisSoundness.given
import sturdy.language.tip.analysis.SignAnalysis.{*, given}

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class GradualSignAnalysisTest  extends AnyFlatSpec, Matchers:
  behavior of "Tip sign dai analysis"

  it should "correctly analyze positive signs" in {
    val sourceCode =
      """
main(){
      var x;
      x = 1 + 3;
      return x;
}
    """.stripMargin
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map(), StackedStates())

      val aresult = analysis.failure.fallible(analysis.execute(program))

      1 should be (1)

    }
  }

  it should "correctly analyze while signs" in {
    val sourceCode =
      """
  main(){
        var x;
        x = 0;
        while (input) {
          x = x + 1;
          x = -1;
        }
        return x;
  }
      """.stripMargin
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map(), StackedStates())

      val aresult = analysis.failure.fallible(analysis.execute(program))

      1 should be(1)

    }
  }

  it should "correctly analyze fun signs" in {
    val sourceCode =
      """
      foo(y) {
        return y + 1;
      }

      main(){
        return foo(1) - foo(0);
      }
      """.stripMargin
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map(), StackedStates())

      val aresult = analysis.failure.fallible(analysis.execute(program))

      1 should be(1)

    }
  }

  it should "correctly analyze print" in {
    val sourceCode =
      """
      main(){
        var x;
        x = 1;
        assert(x>1);
        x = 1+2;
        x = x + (x + (1+2));
        x = x - 1;
        return x+1;
      }
      """.stripMargin
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map(), StackedStates())

      val aresult = analysis.failure.fallible(analysis.execute(program))
      println(s"result ${aresult}")
      println(analysis.gl.m)

      println(Parser.unparse(new Elaboration(analysis.gl, analysis.eo).elaborate(program)))
      //println(s"Effect Stack: ${analysis.effectStack}")
      //println(program.funs(0).body.)

      1 should be(1)

    }
  }


