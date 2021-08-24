package sturdy.language.schemelang

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import sturdy.language.schemelang.Literal.*
import sturdy.language.schemelang.Op1Kinds.*
import sturdy.language.schemelang.Op2Kinds.*
import sturdy.language.schemelang.OpVarKinds.*
import sturdy.language.schemelang.Expr.*
import sturdy.language.schemelang.SExprParser.*

import scala.collection.*
import scala.language.implicitConversions




object SchemeExprParser:

  import SExpr.*

  def parse(source: String): List[Expr] = SExprParser.parse(source).map(transform)

  def transform(se: SExpr): Expr = se match
    case sexpr(es) => transformExprs(es)
    case nonsexpr => transformNonSExpr(nonsexpr)

  def transformNonSExpr(se: SExpr): Expr = se match
    case integer(i1) => Lit(IntLit(i1))
    case rational(i1,i2) => Lit(RationalLit(i1,i2))
    case double(d1) => Lit(DoubleLit(d1))
    case boolean(b1) => Lit(BoolLit(b1))
    case string(s1) =>
      if s1.startsWith("#\\") then Lit(CharLit(s1.toCharArray.last))
      else if s1.startsWith("#\\x") then Lit(CharLit(s1.toCharArray.last))
      else Lit(StringLit(s1))
    case id(x) => Var(x)
    case _ => throw new IllegalArgumentException(s"literal not matched: $se")



  def liftVar(se: SExpr): String = se match
    case id(x) => x
    case _ => throw new IllegalArgumentException(s"expected id but got $se")

  def transformExprs(ses: List[SExpr]): Expr = ses match
    // Nil
    // Cons
    // Begin
    // Var
    // Set_
    case List(id("set!"), id(name), body) => Set_(name, transform(body))
    // Define
    case List(id("define"), id(name), body) => Define(name, transform(body))
    case List(id("define"), sexpr(id(name)::args), body) => Define(name, Lam(args.map(liftVar), List(transform(body))))
    // Lam
    case List(id("lambda"), sexpr(args), body) =>
      val argsS = args.map({ case id(x) => x })
      Lam(argsS, List(transform(body)))
    // If
    case List(id("if"),e1,e2,e3) => If(transform(e1), transform(e2), transform(e3))
    // Let
    case id("let")::sexpr(args)::body =>
      val argsE = args.map({ case sexpr(List(id(name),se)) => (name, transform(se))})
      Let(argsE, body.map(transform))
    // LetRec
    // Op1, missing: random
    case List(id("number?"),e) => Op1(IsNumber, transform(e))
    case List(id("integer?"),e) => Op1(IsInteger, transform(e))
    case List(id("real?"),e) => Op1(IsDouble, transform(e))
    case List(id("rational?"),e) => Op1(IsRational, transform(e))
    case List(id("zero?"),e) => Op1(IsZero, transform(e))
    case List(id("positive?"),e) => Op1(IsPositive, transform(e))
    case List(id("negative?"),e) => Op1(IsNegative, transform(e))
    case List(id("odd?"),e) => Op1(IsOdd, transform(e))
    case List(id("even?"),e) => Op1(IsEven, transform(e))
    case List(id("null?"),e) => Op1(IsNull, transform(e))
    case List(id("cons?"),e) => Op1(IsCons, transform(e))
    case List(id("boolean?"),e) => Op1(IsBoolean, transform(e))
    case List(id("abs"),e) => Op1(Abs, transform(e))
    case List(id("floor"),e) => Op1(Floor, transform(e))
    case List(id("ceiling"),e) => Op1(Ceiling, transform(e))
    case List(id("log"),e) => Op1(Log, transform(e))
    case List(id("not"),e) => Op1(Not, transform(e))
    case List(id("car"),e) => Op1(Car, transform(e))
    case List(id("cdr"),e) => Op1(Cdr, transform(e))
    case List(id("caar"),e) => Op1(Caar, transform(e))
    case List(id("cadr"),e) => Op1(Cadr, transform(e))
    case List(id("cddr"),e) => Op1(Cddr, transform(e))
    case List(id("cadrr"),e) => Op1(Caddr, transform(e))
    case List(id("cadddr"),e) => Op1(Cadddr, transform(e))
    case List(id("number->string"),e) => Op1(NumberToString, transform(e))
    case List(id("string->symbol"),e) => Op1(StringToSymbol, transform(e))
    case List(id("symbol->string"),e) => Op1(SymbolToString, transform(e))
    // Op2, missing StringRef
    case List(id("eq?"),e1,e2) => Op2(Eqv, transform(e1), transform(e2))
    case List(id("eqv?"),e1,e2) => Op2(Eqv, transform(e1), transform(e2))
    case List(id("quotient"),e1,e2) => Op2(Quotient, transform(e1), transform(e2))
    case List(id("remainder"),e1,e2) => Op2(Remainder, transform(e1), transform(e2))
    case List(id("%"),e1,e2) => Op2(Modulo, transform(e1), transform(e2))
    // OpVar
    case id("=")::rest => OpVar(Equal, rest.map(transform))
    case id("<")::rest => OpVar(Smaller, rest.map(transform))
    case id(">")::rest => OpVar(Greater, rest.map(transform))
    case id("<=")::rest => OpVar(SmallerEqual, rest.map(transform))
    case id(">=")::rest => OpVar(GreaterEqual, rest.map(transform))
    case id("max")::rest => OpVar(Max, rest.map(transform))
    case id("min")::rest => OpVar(Min, rest.map(transform))
    case id("+")::rest => OpVar(Add, rest.map(transform))
    case id("*")::rest => OpVar(Mul, rest.map(transform))
    case id("-")::rest => OpVar(Sub, rest.map(transform))
    case id("/")::rest => OpVar(Div, rest.map(transform))
    case id("gcd")::rest => OpVar(Gcd, rest.map(transform))
    case id("lcm")::rest => OpVar(Lcm, rest.map(transform))
    case id("string-append")::rest => OpVar(StringAppend, rest.map(transform))
    case id("or")::x::xs => If(Op2(Eqv, transform(x), Lit(BoolLit(true))), Lit(BoolLit(true)), transform(sexpr(id("or")::xs)))
    case id("or")::Nil => Lit(BoolLit(false))

    // App
    case id(procVar)::args => AppFoo(Var(procVar), args.map(transform))





//    case sexpr(number(n1)::id(".")::number(n2)::Nil) => Lit(DoubleLit((n1.toString++"."++n2.toString).toDouble))
//    case sexpr(number(n1)::id("/")::number(n2)::Nil) => Lit(RationalLit(n1,n2))
//    case sexpr(number(n1)::Nil) => Lit(IntLit(n1))
    case _ => throw new IllegalArgumentException(s"Not matched: $ses")


