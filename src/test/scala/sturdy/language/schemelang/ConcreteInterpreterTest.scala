package sturdy.language.schemelang

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.schemelang.ConcreteInterpreter.*
import sturdy.language.schemelang.ConcreteInterpreter.Value.*
import sturdy.language.schemelang.ConcreteInterpreter.Num.*
import sturdy.language.schemelang.ConcreteInterpreter.Cons.*
import sturdy.language.schemelang.GenericInterpreter.IllegalArgument
import sturdy.values.ints.IntDivisionByZero
import sturdy.language.schemelang.Literal.*
import sturdy.language.schemelang.Expr.*
import sturdy.language.schemelang.Op1Kinds.*
import sturdy.language.schemelang.Op2Kinds.*
import sturdy.language.schemelang.OpVarKinds.*

class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:
  def run(es: List[Expr]): Value =
    val interp = ConcreteInterpreter(Map(), Map())
    interp.run(es)

  "concrete interpreter" should "run ex1" in {
    for _ <- 0 until 10 do
      val res = run(Examples.ex1)
      assertResult(BoolVal(true))(res)
  }

  it should "run ex2" in {
    for _ <- 0 until 10 do
      val res = run(Examples.ex2)
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test set! var" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Define("x", Lit(IntLit(2))),
        Set_("x", Lit(IntLit(3))),
        Var("x")
      ))
      assertResult(NumVal(IntVal(3)))(res)
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
      assertResult(NumVal(IntVal(3)))(res)
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
      assertResult(NumVal(IntVal(4)))(res)
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
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test simple letrec" in {
    for _ <- 0 until 10 do
      val res = run(List(
        LetRec(List(("y", Lit(IntLit(1)))),
          List(Var("y")))
      ))
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test need for stepwise evaluation of letrec bindings" in {
    for _ <- 0 until 10 do
      val res = run(List(
        LetRec(List(("x", Lit(IntLit(4))),
          ("y", Var("x"))),
          List(Var("y")))
      ))
      assertResult(NumVal(IntVal(4)))(res)
  }

  it should "test closure creation" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Lam(List("x"), List(Var("x")))
      ))
      assertResult(ClosureVal((List("x"), Map(), List(Var("x")))))(res)

  }

  it should "test simple closure application" in {
    for _ <- 0 until 10 do
      val res = run(List(
        AppFoo(
          Lam(List("x"), List(Var("x"))),
          List(Lit(IntLit(1))))
      ))
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test letrec with lambda body" in {
    for _ <- 0 until 10 do
      val res = run(List(
        LetRec(
          List(("y", Lam(List("y"), List(Var("y"))))),
          List(AppFoo(Var("y"), List(Lit(IntLit(3))))))
      ))
      assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test let with lambda body" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Let(
          List(("y", Lam(List("y"), List(Var("y"))))),
          List(AppFoo(Var("y"), List(Lit(IntLit(3))))))
      ))
      assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test define with lambda body" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Define("y", Lam(List("y"), List(Var("y")))),
        AppFoo(Var("y"), List(Lit(IntLit(3))))
      ))
      assertResult(NumVal(IntVal(3)))(res)
  }

  it should "illustrate necessity of store " in {
    for _ <- 0 until 10 do
      val res = run(List(
        Define("x", Lit(IntLit(2))),
        AppFoo(Lam(List(), List(Set_("x", Lit(IntLit(3))))), List()),
        Var("x")
      ))
      assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test self referencing letrec" in {
    for _ <- 0 until 10 do
      val res = run(List(
        LetRec(
          List(("y", Lam(List(), List(Var("x")))),
               ("x", Lit(IntLit(2)))),
          List(AppFoo(Var("y"), List())))
      ))
      assertResult(NumVal(IntVal(2)))(res)
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
      assertResult(NumVal(IntVal(10)))(res)
  }

  it should "test < on multiple ints #t (< checks wether the elements are in ascending order) " in {
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

  it should "test < on multiple ints #f (< checks wether the elements are in ascending order) " in {
    for _ <- 0 until 10 do
      val res = run(List(
        OpVar(Smaller, List(
          Lit(IntLit(1)),
          Lit(IntLit(2)),
          Lit(IntLit(2)),
          Lit(IntLit(4))))
      ))
      assertResult(BoolVal(false))(res)
  }

  it should "test lt on mixed ints doubles" in {
    for _ <- 0 until 10 do
      val res = run(List(
        OpVar(Smaller, List(
          Lit(IntLit(1)),
          Lit(IntLit(2)),
          Lit(DoubleLit(3.0)),
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
      assertResult(NumVal(IntVal(-1)))(res)
  }

  it should "test add on multiple doubles" in {
    for _ <- 0 until 10 do
      val res = run(List(
        OpVar(Add, List(
          Lit(DoubleLit(1.1)),
          Lit(DoubleLit(1.1)),
          Lit(DoubleLit(1.1)),
          Lit(DoubleLit(1.1))))
        ))
      assertResult(NumVal(DoubleVal(4.4)))(res)
  }

  it should "test add on multiple ints" in {
    for _ <- 0 until 10 do
      val res = run(List(
        OpVar(Add, List(
          Lit(IntLit(1)),
          Lit(IntLit(1)),
          Lit(IntLit(1)),
          Lit(IntLit(1))))
      ))
      assertResult(NumVal(IntVal(4)))(res)
  }

  it should "test add on mixed ints and doubles" in {
    for _ <- 0 until 10 do
      val res = run(List(
        OpVar(Add, List(
          Lit(IntLit(1)),
          Lit(IntLit(1)),
          Lit(DoubleLit(1.0)),
          Lit(DoubleLit(1.0))))
      ))
      assertResult(NumVal(DoubleVal(4.0)))(res)
  }

  it should "test modulo on ints" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Op2(Modulo, Lit(IntLit(15)), Lit(IntLit(3)))
      ))
      assertResult(NumVal(IntVal(0)))(res)
  }

  it should "test modulo on doubles" in {
    val res = intercept[CFailureException] {
      run(List(
        Op2(Modulo, Lit(DoubleLit(15)), Lit(IntLit(3)))
      ))
    }
    assertResult(CFailureException(
      IllegalArgument,
      "(withInt2): expected int as first argument but got NumVal(DoubleVal(15.0))"))(res)
  }

  it should "test quotient on ints" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Op2(Quotient, Lit(IntLit(1)), Lit(IntLit(3)))
      ))
      assertResult(NumVal(IntVal(0)))(res)
  }

  it should "test int division by 0" in {
    val thrown = intercept[CFailureException] {
      run(List(
        OpVar(Div, List(
        Lit(IntLit(1)), Lit(IntLit(0))))
      ))
    }
    assertResult(thrown)(CFailureException(IntDivisionByZero, "1 / 0"))
  }

  it should "test int abs with type dispatch in generic interp" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Op1(Abs, Lit(IntLit(-1)))
      ))
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test double abs with type dispatch in generic interp" in {
    for _ <- 0 until 10 do
      val res = run(List(
        Op1(Abs, Lit(DoubleLit(-1.0)))
      ))
      assertResult(NumVal(DoubleVal(1.0)))(res)
  }

  it should "test max on multiple ints" in {
    val res = run(List(
      OpVar(Max, List(
        Lit(IntLit(1)),
        Lit(IntLit(0)),
        Lit(IntLit(3))))
    ))
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test max on multiple doubles" in {
    val res = run(List(
      OpVar(Max, List(
        Lit(DoubleLit(1)),
        Lit(DoubleLit(0)),
        Lit(DoubleLit(3))))
    ))
    assertResult(NumVal(DoubleVal(3)))(res)
  }

  it should "test max on mixed int doubles" in {
    val res = run(List(
      OpVar(Max, List(
        Lit(DoubleLit(1)),
        Lit(IntLit(0)),
        Lit(DoubleLit(3))))
    ))
    assertResult(NumVal(DoubleVal(3)))(res)
  }

  it should "test add on rationals" in {
    val res = run(List(
      OpVar(Add, List(
        Lit(RationalLit(1, 7)),
        Lit(RationalLit(1, 7)),
        Lit(RationalLit(1, 7))))
    ))
    assertResult(NumVal(RatioVal(3, 7)))(res)
  }

  it should "test add on mixed ints rationals " in {
    val res = run(List(
      OpVar(Add, List(
        Lit(RationalLit(1,6)),
        Lit(RationalLit(1,6)),
        Lit(IntLit(4))))
    ))
    assertResult(NumVal(RatioVal(13,3)))(res)
  }

  it should "test mul on mixed ints rationals " in {
    val res = run(List(
      OpVar(Mul, List(
        Lit(RationalLit(1,6)),
        Lit(RationalLit(1,6)),
        Lit(IntLit(4))))
    ))
    assertResult(NumVal(RatioVal(1,9)))(res)
  }

  it should "test cadr on list expression " in {
    val res = run(List(
      Op1(Cadr,
        Cons_(Lit(IntLit(1)),
          Cons_(Lit(IntLit(2)),
            Cons_(Lit(IntLit(3)), Nil_))))
    ))
    assertResult(NumVal(IntVal(2)))(res)
  }

  it should "test creation of list expression " in {
    val res = run(List(
        Cons_(Lit(IntLit(1)),
          Cons_(Lit(IntLit(2)),
            Cons_(Lit(IntLit(3)), Nil_)))
    ))
    assertResult(
      ListVal(ConsVal(NumVal(IntVal(1)),
          ListVal(ConsVal(NumVal(IntVal(2)),
              ListVal(ConsVal(NumVal(IntVal(3)),
                ListVal(NilVal))))))))(res)
  }

  it should "test appending of string " in {
    val res = run(List(
      (OpVar(StringAppend, List(
        Lit(StringLit("1")),
          Lit(StringLit("2")),
            Lit(StringLit("3")))))
    ))
    assertResult(StringVal("123"))(res)
  }