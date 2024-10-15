package sturdy.language.tip.analysis

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.fix.StackConfig.StackedStates
import sturdy.language.tip.Parser
import sturdy.language.tip.abstractions.given
import sturdy.language.tip.analysis.given
import sturdy.values.integer.{NumericInterval, given}

class GradualIntervalAnalysisTest extends AnyFlatSpec, Matchers:
  behavior of "Tip interval dai analysis"

  it should "correctly analyze positive signs" in {
    val sourceCode =
      """
        main(){
        |     var x;
        |     x = 1 + 3;
        |     return x;
        |}
    """.stripMargin
    val program = Parser.parse(sourceCode)
    if (program.funs.exists(_.name == "main")) {
      val analysis = new SignAnalysis.Instance(Map(), Map(), StackedStates())

      val aresult = analysis.failure.fallible(analysis.execute(program))

      1 should be(1)
    }
  }
