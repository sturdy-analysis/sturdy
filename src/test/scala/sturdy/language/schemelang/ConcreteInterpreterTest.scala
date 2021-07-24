package sturdy.language.schemelang

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.schemelang.ConcreteInterpreter.*
import sturdy.language.schemelang.ConcreteInterpreter.Value.*
import sturdy.language.schemelang.Literal.*
import sturdy.language.schemelang.Expr.*
import sturdy.language.schemelang.Op1Kinds.*
import sturdy.language.schemelang.OpVarKinds.*

class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:
  def run(es: List[Expr]): Value =
    val interp = ConcreteInterpreter(Map(), Map())
    interp.runFixed(es)

  "concrete interpreter" should "run ex1" in {
    for _ <- 0 until 10 do
      val res = run(Examples.ex1)
      assertResult(BoolVal(true))(res)
  }

  it should "run ex2" in {
    for _ <- 0 until 10 do
      val res = run(Examples.ex2)
      assertResult(IntVal(1))(res)
  }

  it should "test set! var" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Define("x", Lit(IntLit(2))),
        Set_("x", Lit(IntLit(3))),
        Var("x")
      ))
      assertResult(IntVal(3))(res)
  }

  it should "test set! void" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Define("x", Lit(IntLit(2))),
        Set_("x", Lit(IntLit(3)))
      ))
      assertResult(VoidVal)(res)
  }

  it should "test if true" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Define("x", Lit(IntLit(2))),
        If(Lit(BoolLit(true)),
          Set_("x", Lit(IntLit(3))),
          Set_("x", Lit(IntLit(4)))),
        Var("x")
      ))
      assertResult(IntVal(3))(res)
  }

  it should "test if false" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Define("x", Lit(IntLit(2))),
        If(Lit(BoolLit(false)),
          Set_("x", Lit(IntLit(3))),
          Set_("x", Lit(IntLit(4)))),
        Var("x")
      ))
      assertResult(IntVal(4))(res)
  }

  it should "test isNumber true" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Op1(IsNumber, Lit(IntLit(1)))
      ))
      assertResult(BoolVal(true))(res)
  }

  it should "test isNumber false" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Op1(IsNumber, Lit(StringLit("test")))
      ))
      assertResult(BoolVal(false))(res)
  }

  it should "test simple let" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Let(List(("y", Lit(IntLit(1)))),
            List(Var("y")))
      ))
      assertResult(IntVal(1))(res)
  }

  it should "test simple letrec" in {
    for _ <- 0 until 10 do
      val res = run(List(
        LetRec(List(("y", Lit(IntLit(1)))),
          List(Var("y")))
      ))
      assertResult(IntVal(1))(res)
  }

  it should "test need for stepwise evaluation of letrec bindings" in {
    for _ <- 0 until 10 do
      val res = run(List(
        LetRec(List(("x", Lit(IntLit(4))),
          ("y", Var("x"))),
          List(Var("y")))
      ))
      assertResult(IntVal(4))(res)
  }

  it should "test closure creation" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Lam(List("x"), List(Var("x")))
      ))
      assertResult(ClosureVal((Lam(List("x"), List(Var("x"))), Map())))(res)
  }

  it should "test simple closure application" in {
    for _ <- 0 until 10 do
      val res = run(List(
        AppFoo(
          Lam(List("x"), List(Var("x"))),
          List(Lit(IntLit(1))))
      ))
      assertResult(IntVal(1))(res)
  }

  it should "test letrec with lambda body" in {
    for _ <- 0 until 10 do
      val res = run(List(
        LetRec(
          List(("y", Lam(List("y"), List(Var("y"))))),
          List(AppFoo(Var("y"), List(Lit(IntLit(3))))))
      ))
      assertResult(IntVal(3))(res)
  }

  it should "test let with lambda body" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Let(
          List(("y", Lam(List("y"), List(Var("y"))))),
          List(AppFoo(Var("y"), List(Lit(IntLit(3))))))
      ))
      assertResult(IntVal(3))(res)
  }

  it should "test define with lambda body" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Define("y", Lam(List("y"), List(Var("y")))),
        AppFoo(Var("y"), List(Lit(IntLit(3))))
      ))
      assertResult(IntVal(3))(res)
  }

  it should "illustrate necessity of store " in {
    for _ <- 0 until 10 do
      val res = run(List(
        Define("x", Lit(IntLit(2))),
        AppFoo(Lam(List(), List(Set_("x", Lit(IntLit(3))))), List()),
        Var("x")
      ))
      assertResult(IntVal(3))(res)
  }

  it should "test self referencing letrec" in {
    for _ <- 0 until 10 do
      val res = run(List(
        LetRec(
          List(("y", Lam(List(), List(Var("x")))),
               ("x", Lit(IntLit(2)))),
          List(Var("y")))
      ))
      assertResult(IntVal(2))(res)
  }

  it should "test addition of multiple ints" in {
    for _ <- 0 until 10 do
      val res = run(List(
        OpVar(Add, List(
          Lit(IntLit(1)),
          Lit(IntLit(2)),
          Lit(IntLit(3)),
          Lit(IntLit(4))))
      ))
      assertResult(IntVal(10))(res)
  }

  it should "test < on multiple ints (< checkts wether the first arg is smaller than all others) " in {
    for _ <- 0 until 10 do
      val res = run(List(
        OpVar(Smaller, List(
          Lit(IntLit(1)),
          Lit(IntLit(2)),
          Lit(IntLit(3)),
          Lit(IntLit(4))))
      ))
      assertResult(BoolVal(true))(res)
  }

  it should "test min on multiple ints" in {
    for _ <- 0 until 10 do
      val res = run(List(
        OpVar(Min, List(
          Lit(IntLit(1)),
          Lit(IntLit(2)),
          Lit(IntLit(0)),
          Lit(IntLit(-1))))
      ))
      assertResult(IntVal(-1))(res)
  }