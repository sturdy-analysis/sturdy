package sturdy.language.whilelang.analysis

import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.given
import sturdy.language.whilelang.ConcreteInterpreter
import sturdy.language.whilelang.Examples
import sturdy.language.whilelang.Statement
import sturdy.language.whilelang.Statement.*
import sturdy.language.whilelang.analysis.SignAnalysis.*
import sturdy.language.whilelang.analysis.SignAnalysis.Value.*
import sturdy.language.whilelang.analysis.SignAnalysisSoundness.given
import sturdy.util.IntLabel
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.values.Topped.*
import sturdy.values.doubles.DoubleSign

import scala.util.Try

class SignAnalysisTest extends AnyFlatSpec, Matchers:
  def run(s: Statement): SignAnalysis.Effects =
    val analysis = SignAnalysis(Map(), Map())
    analysis.run(s)
    analysis.effectOps

  def testSoundness(s: Statement): Assertion =
    val interp = ConcreteInterpreter(Map(), Map())
    val analysis = SignAnalysis(Map(), Map())
    val cresult = interp.effectOps.fallible(interp.run(s))
    val aresult = analysis.effectOps.fallible(analysis.run(s))
    assertResult(IsSound.Sound)(Soundness.isSound(cresult, aresult))
    assertResult(IsSound.Sound)(Soundness.isSound(interp, analysis))


  "sign analysis" should "run ex1" in {
    val res = run(Examples.ex1)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,yAssign,_)) = Examples.ex1
    assertResult(Some(true -> Powerset(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Powerset(yAssign.label)))(env.get("y"))
    assertResult(Some(DoubleValue(DoubleSign.ZeroOrPos)))(store.get(xAssign.label))
    assertResult(Some(DoubleValue(DoubleSign.ZeroOrPos)))(store.get(yAssign.label))
  }

  it should "run ex2" in {
    val res = run(Examples.ex2)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,If(_,Block(List(yAssign1)),Block(List(yAssign2))))) = Examples.ex2
    assertResult(Some(true -> Powerset(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Powerset(yAssign1.label, yAssign2.label)))(env.get("y"))
    assertResult(Some(DoubleValue(DoubleSign.ZeroOrPos)))(store.get(xAssign.label))
    assertResult(Some(DoubleValue(DoubleSign.Pos)))(store.get(yAssign1.label))
    assertResult(Some(DoubleValue(DoubleSign.Pos)))(store.get(yAssign2.label))
  }

  it should "run ex3" in {
    val res = run(Examples.ex3)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,If(_,Block(List(yAssign1)),Block(List(yAssign2))))) = Examples.ex3
    assertResult(Some(true -> Powerset(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Powerset(yAssign1.label, yAssign2.label)))(env.get("y"))
    assertResult(Some(DoubleValue(DoubleSign.Pos)))(store.get(xAssign.label))
    assertResult(Some(DoubleValue(DoubleSign.Pos)))(store.get(yAssign1.label))
    assertResult(Some(DoubleValue(DoubleSign.Pos)))(store.get(yAssign2.label))
  }

  it should "run ex4" in {
    val res = run(Examples.ex4)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,yAssign)) = Examples.ex4
    assertResult(Some(true -> Powerset(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Powerset(yAssign.label)))(env.get("y"))
    assertResult(Some(DoubleValue(DoubleSign.Zero)))(store.get(xAssign.label))
    assertResult(Some(DoubleValue(DoubleSign.Pos)))(store.get(yAssign.label))
  }

  it should "run ex5" in {
    val res = run(Examples.ex5)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,yAssign)) = Examples.ex5
    assertResult(Some(true -> Powerset(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Powerset(yAssign.label)))(env.get("y"))
    assertResult(Some(DoubleValue(DoubleSign.ZeroOrPos)))(store.get(xAssign.label))
    assertResult(Some(DoubleValue(DoubleSign.Pos)))(store.get(yAssign.label))
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
