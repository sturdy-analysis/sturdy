package sturdy.language.whilelang

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import sturdy.language.whilelang.ConcreteInterpreter._
import sturdy.language.whilelang.ConcreteInterpreter.Value._

class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:
  def run(s: Statement): (Environment, Store) =
    val interp = ConcreteInterpreter(Map(), Map())
    interp.run(s)
    (interp.effectOps.getEnv, interp.effectOps.getStore)

  "concrete interpreter" should "run ex1" in {
    for _ <- 0 until 10 do
      val (env, store) = run(Examples.ex1)
      assertResult(Some(0))(env.get("x"))
      assertResult(Some(1))(env.get("y"))
      val yVal = store.get(1)
      assert(yVal == Some(DoubleValue(1.0)) || yVal == Some(DoubleValue(2.0)))
  }

  it should "run ex2" in {
    for _ <- 0 until 10 do
      val (env, store) = run(Examples.ex2)
      assertResult(Some(0))(env.get("x"))
      assertResult(Some(1))(env.get("y"))
      val yVal = store.get(1)
      assert(yVal == Some(DoubleValue(1.0)) || yVal == Some(DoubleValue(2.0)))
  }

  it should "run ex3" in {
    val (env, store) = run(Examples.ex3)
    assertResult(Some(0))(env.get("x"))
    assertResult(Some(1))(env.get("y"))
    assertResult(Some(DoubleValue(2.0)))(store.get(0))
    assertResult(Some(DoubleValue(2.0)))(store.get(1))
  }
