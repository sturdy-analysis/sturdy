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
  def run(s: Statement): (Environment, Store) =
    val analysis = SignAnalysis(Map(), Map())
    analysis.run(s)
    (analysis.effectOps.getEnv, analysis.effectOps.getStore)

  "interval analysis" should "run ex1" in {
    val (env, store) = run(Examples.ex1)
    val Block(List(xAssign,yAssign,_)) = Examples.ex1
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Sign.ZeroOrPos)))(store.get(xAssign.label))
    assertResult(Some(true -> DoubleValue(Sign.Pos)))(store.get(yAssign.label))
  }

  it should "run ex2" in {
    val (env, store) = run(Examples.ex2)
    val Block(List(xAssign,If(_,Block(List(yAssign1)),Block(List(yAssign2))))) = Examples.ex2
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign1.label, yAssign2.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Sign.ZeroOrPos)))(store.get(xAssign.label))
    assertResult(Some(false -> DoubleValue(Sign.Pos)))(store.get(yAssign1.label))
    assertResult(Some(false -> DoubleValue(Sign.Pos)))(store.get(yAssign2.label))
  }

  it should "run ex3" in {
    val (env, store) = run(Examples.ex3)
    val Block(List(xAssign,If(_,Block(List(yAssign1)),Block(List(yAssign2))))) = Examples.ex3
    assertResult(Some(true -> Set(xAssign.label)))(env.get("x"))
    assertResult(Some(true -> Set(yAssign1.label, yAssign2.label)))(env.get("y"))
    assertResult(Some(true -> DoubleValue(Sign.Pos)))(store.get(xAssign.label))
    assertResult(Some(false -> DoubleValue(Sign.Pos)))(store.get(yAssign1.label))
    assertResult(Some(false -> DoubleValue(Sign.Pos)))(store.get(yAssign2.label))
  }
