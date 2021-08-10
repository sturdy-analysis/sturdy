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
import sturdy.util.*
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.values.references.*
import sturdy.values.Topped.*
import sturdy.values.doubles.DoubleInterval

class IntervalAnalysisTest extends AnyFlatSpec, Matchers:
  def run(s: Statement): IntervalAnalysis.Effects =
    val analysis = IntervalAnalysis(Map(), Map())
    analysis.run(s)
    analysis.effectOps

  def Addr(l: Labeled) = AllocationSiteAddr.Alloc(l.label)(true)
  def Addr(l: Label) = AllocationSiteAddr.Alloc(l)(true)

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
    assertResult(Some(MayMust.Must(Powerset(Addr(xAssign)))))(env.get("x"))
    assertResult(Some(MayMust.Must(Powerset(Addr(yAssign)))))(env.get("y"))
    assertResult(Some(DoubleValue(DoubleInterval(0.0, 1.0))))(store.get(Addr(xAssign)))
    assertResult(Some(DoubleValue(DoubleInterval(0.0, 2.0))))(store.get(Addr(yAssign)))
  }

  it should "run ex2" in {
    val res = run(Examples.ex2)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,If(_,Block(List(yAssign1)),Block(List(yAssign2))))) = Examples.ex2
    assertResult(Some(MayMust.Must(Powerset(Addr(xAssign)))))(env.get("x"))
    assertResult(Some(MayMust.Must(Powerset(Addr(yAssign1), Addr(yAssign2)))))(env.get("y"))
    assertResult(Some(DoubleValue(DoubleInterval(0.0, 1.0))))(store.get(Addr(xAssign.label)))
    assertResult(Some(DoubleValue(DoubleInterval(1.0, 1.0))))(store.get(Addr(yAssign1.label)))
    assertResult(Some(DoubleValue(DoubleInterval(2.0, 2.0))))(store.get(Addr(yAssign2.label)))
  }

  it should "run ex3" in {
    val res = run(Examples.ex3)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,If(_,Block(List(yAssign)),_))) = Examples.ex3
    assertResult(Some(MayMust.Must(Powerset(Addr(xAssign)))))(env.get("x"))
    assertResult(Some(MayMust.Must(Powerset(Addr(yAssign)))))(env.get("y"))
    assertResult(Some(DoubleValue(DoubleInterval(2.0, 2.0))))(store.get(Addr(xAssign.label)))
    assertResult(Some(DoubleValue(DoubleInterval(2.0, 2.0))))(store.get(Addr(yAssign.label)))
  }

  it should "run ex4" in {
    val res = run(Examples.ex4)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign, yAssign)) = Examples.ex4
    assertResult(Some(MayMust.Must(Powerset(Addr(xAssign)))))(env.get("x"))
    assertResult(Some(MayMust.Must(Powerset(Addr(yAssign)))))(env.get("y"))
    assertResult(Some(DoubleValue(DoubleInterval(0.0, 0.0))))(store.get(Addr(xAssign.label)))
    assertResult(Some(DoubleValue(DoubleInterval(Double.PositiveInfinity, Double.PositiveInfinity))))(store.get(Addr(yAssign.label)))
  }

  it should "run ex5" in {
    val res = run(Examples.ex5)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign, yAssign)) = Examples.ex5
    assertResult(Some(MayMust.Must(Powerset(Addr(xAssign)))))(env.get("x"))
    assertResult(Some(MayMust.Must(Powerset(Addr(yAssign)))))(env.get("y"))
    assertResult(Some(DoubleValue(DoubleInterval(0.0, 1.0))))(store.get(Addr(xAssign.label)))
    assertResult(Some(DoubleValue(DoubleInterval(5.0, Double.PositiveInfinity))))(store.get(Addr(yAssign.label)))
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