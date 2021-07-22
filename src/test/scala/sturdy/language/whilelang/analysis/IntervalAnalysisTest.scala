package sturdy.language.whilelang.analysis

import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.given
import sturdy.language.whilelang.ConcreteInterpreter
import sturdy.language.whilelang.{Statement, Examples}
import sturdy.language.whilelang.Statement.*
import sturdy.language.whilelang.analysis.IntervalAnalysis.*
import sturdy.language.whilelang.analysis.IntervalAnalysis.Value.*
import sturdy.language.whilelang.analysis.IntervalAnalysisSoundness.given
import sturdy.util.IntLabel
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.values.Topped.*
import sturdy.values.doubles.DoubleInterval

class IntervalAnalysisTest extends AnyFlatSpec, Matchers:
  def run(s: Statement): IntervalAnalysis.Effects =
    val analysis = IntervalAnalysis(Map(), Map())
    analysis.run(s)
    analysis.effectOps

  def testSoundness(s: Statement): Assertion =
    val interp = ConcreteInterpreter(Map(), Map())
    val analysis = IntervalAnalysis(Map(), Map())
    val cresult = interp.effectOps.fallible(interp.run(s))
    val aresult = analysis.effectOps.fallible(analysis.run(s))
    assertResult(IsSound.Sound)(Soundness.isSound(cresult, aresult))
    assertResult(IsSound.Sound)(Soundness.isSound(interp, analysis))

  "interval analysis" should "run ex1" in {
    val res = run(Examples.ex1)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,yAssign,_)) = Examples.ex1
    assertResult(Some(true -> Powerset(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Powerset(yAssign.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(0.0, 1.0)))))(store.get(xAssign.label))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(1.0, 2.0)))))(store.get(yAssign.label))
  }

  it should "run ex2" in {
    val res = run(Examples.ex2)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,If(_,Block(List(yAssign1)),Block(List(yAssign2))))) = Examples.ex2
    assertResult(Some(true -> Powerset(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Powerset(yAssign1.label, yAssign2.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(0.0, 1.0)))))(store.get(xAssign.label))
    assertResult(Some(false -> DoubleValue(Actual(DoubleInterval(1.0, 1.0)))))(store.get(yAssign1.label))
    assertResult(Some(false -> DoubleValue(Actual(DoubleInterval(2.0, 2.0)))))(store.get(yAssign2.label))
  }

  it should "run ex3" in {
    val res = run(Examples.ex3)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,If(_,Block(List(yAssign)),_))) = Examples.ex3
    assertResult(Some(true -> Powerset(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Powerset(yAssign.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(2.0, 2.0)))))(store.get(xAssign.label))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(2.0, 2.0)))))(store.get(yAssign.label))
  }

  it should "run ex4" in {
    val res = run(Examples.ex4)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign, yAssign)) = Examples.ex4
    assertResult(Some(true -> Powerset(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Powerset(yAssign.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(0.0, 0.0)))))(store.get(xAssign.label))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(Double.PositiveInfinity, Double.PositiveInfinity)))))(store.get(yAssign.label))
  }

  it should "run ex5" in {
    val res = run(Examples.ex5)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign, yAssign)) = Examples.ex5
    assertResult(Some(true -> Powerset(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Powerset(yAssign.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(0.0, 1.0)))))(store.get(xAssign.label))
    assertResult(Some(true -> DoubleValue(Actual(DoubleInterval(5.0, Double.PositiveInfinity)))))(store.get(yAssign.label))
  }

  it should "soundly abstract ex1" in {
    testSoundness(Examples.ex1)
  }
  it should "soundly abstract ex2" in {
    testSoundness(Examples.ex2)
  }
  it should "soundly abstract ex3" in {
    testSoundness(Examples.ex3)
  }
  it should "soundly abstract ex4" in {
    testSoundness(Examples.ex4)
  }