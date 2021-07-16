package sturdy.language.whilelang.analysis

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.whilelang.Examples
import sturdy.language.whilelang.Statement
import sturdy.language.whilelang.Statement.*
import sturdy.language.whilelang.analysis.SignAnalysis.*
import sturdy.language.whilelang.analysis.SignAnalysis.Value.*
import sturdy.util.IntLabel
import sturdy.values.Topped
import sturdy.values.Topped.*
import sturdy.values.domain.Sign

class SignAnalysisTest extends AnyFlatSpec, Matchers:
  def run(s: Statement): SignAnalysis.Effects =
    val analysis = SignAnalysis(Map(), Map())
    analysis.run(s)
    analysis.effectOps

  "sign analysis" should "run ex1" in {
    val res = run(Examples.ex1)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,yAssign,_)) = Examples.ex1
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Sign.ZeroOrPos)))(store.get(xAssign.label))
    assertResult(Some(true -> DoubleValue(Sign.Pos)))(store.get(yAssign.label))
  }

  it should "run ex2" in {
    val res = run(Examples.ex2)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,If(_,Block(List(yAssign1)),Block(List(yAssign2))))) = Examples.ex2
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign1.label, yAssign2.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Sign.ZeroOrPos)))(store.get(xAssign.label))
    assertResult(Some(false -> DoubleValue(Sign.Pos)))(store.get(yAssign1.label))
    assertResult(Some(false -> DoubleValue(Sign.Pos)))(store.get(yAssign2.label))
  }

  it should "run ex3" in {
    val res = run(Examples.ex3)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,If(_,Block(List(yAssign1)),Block(List(yAssign2))))) = Examples.ex3
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign1.label, yAssign2.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Sign.Pos)))(store.get(xAssign.label))
    assertResult(Some(false -> DoubleValue(Sign.Pos)))(store.get(yAssign1.label))
    assertResult(Some(false -> DoubleValue(Sign.Pos)))(store.get(yAssign2.label))
  }

  it should "run ex4" in {
    val res = run(Examples.ex4)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,yAssign)) = Examples.ex4
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Sign.Zero)))(store.get(xAssign.label))
    assertResult(Some(true -> DoubleValue(Sign.Pos)))(store.get(yAssign.label))
  }

  it should "run ex5" in {
    val res = run(Examples.ex5)
    val env = res.getEnv
    val store = res.getStore
    val Block(List(xAssign,yAssign)) = Examples.ex5
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Sign.ZeroOrPos)))(store.get(xAssign.label))
    assertResult(Some(true -> DoubleValue(Sign.Pos)))(store.get(yAssign.label))
  }