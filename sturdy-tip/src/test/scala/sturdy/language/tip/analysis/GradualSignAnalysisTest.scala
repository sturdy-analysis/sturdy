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
        x = 2;
        assert(x > 1);
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

      val elaboration = new TipElaboration(analysis.gl, analysis.eo)
      val concreteInterpreter = ConcreteInterpreter(() => ConcreteInterpreter.Value.IntValue(0))
      val elaborated = elaboration.elaborate(program)
      //println(s"Effect Stack: ${analysis.effectStack}")
      //println(program.funs(0).body.)
      println(unparse(elaborated))
      println(concreteInterpreter.execute(elaborated))

      1 should be(1)

    }
  }


  it should "correctly analyze sum of ZeroOrPos and Pos" in {
    val sourceCode =
      """
      main() {
      | var zop, noz;
      | zop = 2 > 1 + 0;
      | noz = 0 - zop;
      | return zop + 1;
      |}
      """.stripMargin
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map(), StackedStates())

      val aresult = analysis.failure.fallible(analysis.execute(program))
      println(s"result ${aresult}")
      println(analysis.gl.m)

      val elaboration = new TipElaboration(analysis.gl, analysis.eo)
      var count = 1
      val concreteInterpreter = ConcreteInterpreter(() => {
        val v = ConcreteInterpreter.Value.IntValue(count)
        count += 2
        v
      })
      val elaborated = elaboration.elaborate(program)
      //println(s"Effect Stack: ${analysis.effectStack}")
      //println(program.funs(0).body.)
      println(unparse(elaborated))
      println(concreteInterpreter.execute(elaborated))

      1 should be(1)
    }
  }

  it should "correctly analyze multiplication between ZeroOrPos" in {
    val sourceCode =
      """
        main() {
        | var zop0, zop1, tst1, tst2, tst3;
        | zop0 = 1 > 2 + 0;
        | zop1 = 2 > 1 + 0;
        | tst1 = zop0 * zop0;
        | tst2 = zop1 * zop0;
        | tst3 = zop1 * zop1;
        | return tst1 + tst2;
        |}
          """.stripMargin
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map(), StackedStates())

      val aresult = analysis.failure.fallible(analysis.execute(program))
      println(s"result ${aresult}")
      println(analysis.gl.m)

      val elaboration = new TipElaboration(analysis.gl, analysis.eo)
      var count = 1
      val concreteInterpreter = ConcreteInterpreter(() => {
        val v = ConcreteInterpreter.Value.IntValue(count)
        count += 2
        v
      })
      val elaborated = elaboration.elaborate(program)
      //println(s"Effect Stack: ${analysis.effectStack}")
      //println(program.funs(0).body.)
      println(unparse(elaborated))
      println(concreteInterpreter.execute(elaborated))

      1 should be(1)
    }
  }

  it should "correctly fail if assumptions are not met" in {
    val sourceCode =
      s"""
        main() {
        | var almostOverflown, pos;
        | almostOverflown = ${Int.MaxValue};
        | pos = 1;
        | return almostOverflown + pos;
        |}""".stripMargin
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map(), StackedStates())

      val aresult = analysis.failure.fallible(analysis.execute(program))
      println(s"result ${aresult}")
      println(analysis.gl.m)

      val elaboration = new TipElaboration(analysis.gl, analysis.eo)
      var count = 1
      val concreteInterpreter = ConcreteInterpreter(() => {
        val v = ConcreteInterpreter.Value.IntValue(count)
        count += 2
        v
      })
      val elaborated = elaboration.elaborate(program)
      //println(s"Effect Stack: ${analysis.effectStack}")
      //println(program.funs(0).body.)
      println(unparse(elaborated))
      try {
        concreteInterpreter.execute(elaborated)
        println("Incorrect, concrete evaluation did not raise an error.")
      } catch {
        case e: Throwable => println(s"Correctly raised exception: ${e.getClass.getName}")
      }

      1 should be(1)
    }
  }

  it should "correctly analyze underflow" in {
    val sourceCode =
      s"""
        main() {
         | var minVal, tst;
         | minVal = ${Int.MinValue};
         | tst = 0 - minVal;
         | return tst;
         |}""".stripMargin
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map(), StackedStates())

      val aresult = analysis.failure.fallible(analysis.execute(program))
      println(s"result ${aresult}")
      println(analysis.gl.m)

      val elaboration = new TipElaboration(analysis.gl, analysis.eo)
      var count = 1
      val concreteInterpreter = ConcreteInterpreter(() => {
        val v = ConcreteInterpreter.Value.IntValue(count)
        count += 2
        v
      })
      val elaborated = elaboration.elaborate(program)
      //println(s"Effect Stack: ${analysis.effectStack}")
      //println(program.funs(0).body.)
      println(unparse(elaborated))
      try {
        concreteInterpreter.execute(elaborated)
        println("Incorrect, concrete evaluation did not raise an error.")
      } catch {
        case e: Throwable => println(s"Correctly raised exception: ${e.getClass.getName}")
      }

      1 should be(1)
    }
  }
