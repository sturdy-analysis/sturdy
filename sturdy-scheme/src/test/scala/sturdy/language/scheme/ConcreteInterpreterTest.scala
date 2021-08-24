package sturdy.language.scheme

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.*
import sturdy.language.scheme.ConcreteInterpreter.*
import sturdy.language.scheme.ConcreteInterpreter.Value.*
import sturdy.language.scheme.ConcreteInterpreter.Num.*
import sturdy.values.ints.IntDivisionByZero
import sturdy.language.scheme.Literal.*
import sturdy.language.scheme.Exp.*
import sturdy.language.scheme.GenericInterpreter.TypeError
import sturdy.language.scheme.Op1Kinds.*
import sturdy.language.scheme.Op2Kinds.*
import sturdy.language.scheme.OpVarKinds.*
import sturdy.values.rationals.Rational
import sturdy.values.closures.Closure

class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:

  def runAny(ds: List[Any]): Value =
    val interp = ConcreteInterpreter(Map(), Map())
    val forms = ds.map {
      case d: Define => Form.Definition(d)
      case e: Exp => Form.Expression(e)
    }
    interp.execute(Program(forms))

  def exec(p: Program): Value =
    val interp = ConcreteInterpreter(Map(), Map())
    interp.execute(p)

  "concrete interpreter" should "run (fac 10)" in {
    val res = exec(Examples.factorial)
    assertResult(NumVal(IntVal(3628800)))(res)
  }

  it should "test set! var" in {
    val res = runAny(List(
      Define("x", Lit(IntLit(2))),
      Set_("x", Lit(IntLit(3))),
      Var("x")
    ))
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test set! void" in {
    val res = runAny(List(
      Define("x", Lit(IntLit(2))),
      Set_("x", Lit(IntLit(3)))
    ))
    assertResult(VoidVal)(res)
  }

  it should "test if true" in {
    val res = runAny(List(
      Define("x", Lit(IntLit(2))),
      If(Lit(BoolLit(true)),
        Set_("x", Lit(IntLit(3))),
        Set_("x", Lit(IntLit(4)))),
      Var("x")
    ))
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test if false" in {
    val res = runAny(List(
      Define("x", Lit(IntLit(2))),
      If(Lit(BoolLit(false)),
        Set_("x", Lit(IntLit(3))),
        Set_("x", Lit(IntLit(4)))),
      Var("x")
    ))
    assertResult(NumVal(IntVal(4)))(res)
  }

  it should "test isNumber true" in {
    val res = runAny(List(
      Op1(IsNumber, Lit(IntLit(1)))
    ))
    assertResult(BoolVal(true))(res)
  }

  it should "test isNumber false" in {
    val res = runAny(List(
      Op1(IsNumber, Lit(StringLit("test")))
    ))
    assertResult(BoolVal(false))(res)
  }

  it should "test simple let" in {
    val res = runAny(List(
      Let(List(("y", Lit(IntLit(1)))),
        Body(Var("y")))
    ))
    assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test simple LetRec" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        LetRec(List(("y", Lit(IntLit(1)))),
          Body(Var("y")))
      ))
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test need for stepwise evaluation of LetRec bindings" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        LetRec(List(("x", Lit(IntLit(4))),
          ("y", Var("x"))),
          Body(Var("y")))
      ))
      assertResult(NumVal(IntVal(4)))(res)
  }

  it should "test closure creation" in {
    for _ <- 0 until 10 do
      val body = Body(Var("x"))
      val res = runAny(List(
        Lam(List("x"), body)
      ))
      assertResult(ClosureVal(Closure(List("x"), body, Map())))(res)

  }

  it should "test simple closure application" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        Apply(
          Lam(List("x"), Body(Var("x"))),
          List(Lit(IntLit(1))))
      ))
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test LetRec with lambda body" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        LetRec(
          List(("y", Lam(List("y"), Body(Var("y"))))),
          Body(Apply(Var("y"), List(Lit(IntLit(3))))))
      ))
      assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test let with lambda body" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        Let(
          List(("y", Lam(List("y"), Body(Var("y"))))),
          Body(Apply(Var("y"), List(Lit(IntLit(3))))))
      ))
      assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test define with lambda body" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        Define("y", Lam(List("y"), Body(Var("y")))),
        Apply(Var("y"), List(Lit(IntLit(3))))
      ))
      assertResult(NumVal(IntVal(3)))(res)
  }

  it should "illustrate necessity of store " in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        Define("x", Lit(IntLit(2))),
        Apply(Lam(List(), Body(Set_("x", Lit(IntLit(3))))), List()),
        Var("x")
      ))
      assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test self referencing LetRec" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        LetRec(
          List(("y", Lam(List(), Body(Var("x")))),
               ("x", Lit(IntLit(2)))),
          Body(Apply(Var("y"), List())))
      ))
      assertResult(NumVal(IntVal(2)))(res)
  }

  it should "test addition of multiple ints" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
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
      val res = runAny(List(
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
      val res = runAny(List(
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
      val res = runAny(List(
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
      val res = runAny(List(
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
      val res = runAny(List(
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
      val res = runAny(List(
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
      val res = runAny(List(
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
      val res = runAny(List(
        Op2(Modulo, Lit(IntLit(15)), Lit(IntLit(3)))
      ))
      assertResult(NumVal(IntVal(0)))(res)
  }

  it should "test modulo on doubles" in {
    val res = intercept[CFailureException] {
      runAny(List(
        Op2(Modulo, Lit(DoubleLit(15.3)), Lit(IntLit(3)))
      ))
    }
    assertResult(TypeError)(res.kind)
  }

  it should "test quotient on ints" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        Op2(Quotient, Lit(IntLit(1)), Lit(IntLit(3)))
      ))
      assertResult(NumVal(IntVal(0)))(res)
  }

  it should "test int division by 0" in {
    val thrown = intercept[CFailureException] {
      runAny(List(
        OpVar(Div, List(
        Lit(IntLit(1)), Lit(IntLit(0))))
      ))
    }
    assertResult(thrown)(CFailureException(IntDivisionByZero, "1 / 0"))
  }

  it should "test int abs with type dispatch in generic interp" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        Op1(Abs, Lit(IntLit(-1)))
      ))
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test double abs with type dispatch in generic interp" in {
    for _ <- 0 until 10 do
      val res = runAny(List(
        Op1(Abs, Lit(DoubleLit(-1.3)))
      ))
      assertResult(NumVal(DoubleVal(1.3)))(res)
  }

  it should "test max on multiple ints" in {
    val res = runAny(List(
      OpVar(Max, List(
        Lit(IntLit(1)),
        Lit(IntLit(0)),
        Lit(IntLit(3))))
    ))
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test max on multiple doubles" in {
    val res = runAny(List(
      OpVar(Max, List(
        Lit(DoubleLit(1)),
        Lit(DoubleLit(0)),
        Lit(DoubleLit(3))))
    ))
    assertResult(NumVal(DoubleVal(3)))(res)
  }

  it should "test max on mixed int doubles" in {
    val res = runAny(List(
      OpVar(Max, List(
        Lit(DoubleLit(1)),
        Lit(IntLit(0)),
        Lit(DoubleLit(3.3))))
    ))
    assertResult(NumVal(DoubleVal(3.3)))(res)
  }

  it should "test add on rationals" in {
    val res = runAny(List(
      OpVar(Add, List(
        Lit(RationalLit(1, 7)),
        Lit(RationalLit(1, 7)),
        Lit(RationalLit(1, 7))))
    ))
    given Failure = new CFailure {}
    assertResult(NumVal(RationalVal(Rational(3, 7))))(res)
  }

  it should "test add on mixed ints rationals " in {
    val res = runAny(List(
      OpVar(Add, List(
        Lit(RationalLit(1,6)),
        Lit(RationalLit(1,6)),
        Lit(IntLit(4))))
    ))
    given Failure = new CFailure {}
    assertResult(NumVal(RationalVal(Rational(13,3))))(res)
  }

  it should "test mul on mixed ints rationals " in {
    val res = runAny(List(
      OpVar(Mul, List(
        Lit(RationalLit(1,6)),
        Lit(RationalLit(1,6)),
        Lit(IntLit(4))))
    ))
    given Failure = new CFailure {}
    assertResult(NumVal(RationalVal(Rational(1,9))))(res)
  }

  it should "test cadr on list expression " in {
    val res = runAny(List(
      Op1(Cadr,
        Cons_(Lit(IntLit(1)),
          Cons_(Lit(IntLit(2)),
            Cons_(Lit(IntLit(3)), Nil_))))
    ))
    assertResult(NumVal(IntVal(2)))(res)
  }

  it should "test creation of list expression " in {
    val res = runAny(List(
        Cons_(Lit(IntLit(1)),
          Cons_(Lit(IntLit(2)),
            Cons_(Lit(IntLit(3)), Nil_)))
    ))
    assertResult(ConsVal(NumVal(IntVal(1)), ConsVal(NumVal(IntVal(2)), ConsVal(NumVal(IntVal(3)), NilVal))))(res)
  }

  it should "test appending of string " in {
    val res = runAny(List(
      (OpVar(StringAppend, List(
        Lit(StringLit("1")),
          Lit(StringLit("2")),
            Lit(StringLit("3")))))
    ))
    assertResult(StringVal("123"))(res)
  }