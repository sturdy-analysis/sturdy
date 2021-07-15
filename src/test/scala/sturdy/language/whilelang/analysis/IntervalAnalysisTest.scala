package sturdy.language.whilelang.analysis

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.whilelang.{Statement, Examples}
import sturdy.language.whilelang.Statement.*
import sturdy.language.whilelang.analysis.IntervalAnalysis.*
import sturdy.language.whilelang.analysis.IntervalAnalysis.Value.*
import sturdy.util.IntLabel
import sturdy.values.Topped
import sturdy.values.Topped.*
import sturdy.values.domain.DoubleInterval

class IntervalAnalysisTest extends AnyFlatSpec, Matchers:
  def run(s: Statement): (Environment, Store) =
    val analysis = IntervalAnalysis(Map(), Map())
    analysis.run(s)
    (analysis.effectOps.getEnv, analysis.effectOps.getStore)

  "interval analysis" should "run ex1" in {
    val (env, store) = run(Examples.ex1)
    val Block(List(xAssign,yAssign,_)) = Examples.ex1
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(0.0, 1.0)))))(store.get(xAssign.label))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(1.0, 2.0)))))(store.get(yAssign.label))
  }

  it should "run ex2" in {
    val (env, store) = run(Examples.ex2)
    val Block(List(xAssign,If(_,Block(List(yAssign1)),Block(List(yAssign2))))) = Examples.ex2
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign1.label, yAssign2.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(0.0, 1.0)))))(store.get(xAssign.label))
    assertResult(Some(false -> DoubleValue(Actual(DoubleInterval(1.0, 1.0)))))(store.get(yAssign1.label))
    assertResult(Some(false -> DoubleValue(Actual(DoubleInterval(2.0, 2.0)))))(store.get(yAssign2.label))
  }

  it should "run ex3" in {
    val (env, store) = run(Examples.ex3)
    val Block(List(xAssign,If(_,Block(List(yAssign)),_))) = Examples.ex3
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(2.0, 2.0)))))(store.get(xAssign.label))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(2.0, 2.0)))))(store.get(yAssign.label))
  }
