package sturdy.language.scheme


import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import sturdy.language.scheme.SExp.*
import sturdy.language.scheme.Exp.*
import sturdy.language.scheme.Literal.*
import sturdy.language.scheme.Op1Kinds.*
import sturdy.language.scheme.Op2Kinds.*
import sturdy.language.scheme.OpVarKinds.*
import sturdy.language.scheme.Form.*

import scala.collection.*
import scala.language.implicitConversions



object SchemeExpParser:

  def parse(source: String): Program = Program(SExpParser.parse(source).map(transformForm))

  def liftVar(se: SExp): String = se match
    case id(x) => x
    case _ => throw new IllegalArgumentException(s"expected id but got $se")

  def transformForm(se: SExp): Form = se match
    case sexpr(id("define")::rest) => Definition(transformDefine(rest))
    case sexpr(id("begin")::rest) => ???
    case _ => Expression(transformSExp(se))

  def transformDefine(ses: List[SExp]): Define = ses match
    case List(id(name), body) => Define(name, transformSExp(body))
    case List(sexpr(id(name)::args), body) => Define(name, Lam(args.map(liftVar), transformBody(List(body)))) // does this make sense?
    case sexpr(id(name)::args)::body => Define(name, Lam(args.map(liftVar), transformBody(body))) // does this make sense?

  def transformLit(se: SExp): Literal = se match
    case integer(i1) => IntLit(i1)
    case rational(i1, i2) => RationalLit(i1, i2)
    case double(d1) => DoubleLit(d1)
    case boolean(b1) => BoolLit(b1)
    case string(s1) =>
      if s1.startsWith("#\\") then CharLit(s1.toCharArray.last)
      else if s1.startsWith("#\\x") then CharLit(s1.toCharArray.last)
      else StringLit(s1)
    case id(x) => SymbolLit(x)

  def transformQuoted(se: SExp): Exp = se match
    case id(str) => Lit(QuoteLit(SymbolLit(str)))
    case sexpr(Nil) => Nil_
    case sexpr(ls) =>
      // duplicate code ..
      val parsed = ls.map(x => transformQuoted(x))
      Cons_(parsed.head, parsed.tail.foldRight(Nil_) { (x, y) => Cons_(x, y) })
    case _ => Lit(transformLit(se))

  def transformBase(se: SExp): Exp = se match
    case quoted(sexpr(Nil)) => Nil_
    case quoted(sexpr(ls)) => {
      val parsed = ls.map(x => (transformQuoted(x)))
      Cons_(parsed.head, parsed.tail.foldRight(Nil_) { (x, y) => Cons_(x, y) })
    }
    case quoted(base) => Lit(QuoteLit(transformLit(base)))
    case id(x) => Var(x)
    case _ => Lit(transformLit(se))
    case _ => throw new IllegalArgumentException(s"literal not matched: $se")

  def transformBody(ses: List[SExp]): Body = ses match
    case Nil => Body(Nil, Nil) // not allowed, but occurs in one of the benchmarks
    case se::Nil =>
      Body(Nil,List(transformSExp(se)))
    case _ =>
      // there seems to be some issue here
      val parsed = ses.map { x => x match
        case sexpr(id("define")::rest) => Left(transformDefine(rest))
        case _ => Right(transformSExp(x))
      }
      val defs = parsed.collect { case Left(l) => l }
      val es = parsed.collect { case Right(r) => r }
      Body(defs, es)

  def transformSExp(se: SExp): Exp = se match
    case sexpr(ses) => ses match
      // Nil
      case Nil => Nil_
    // Cons
      case List(id("cons"), e1, e2) => Cons_(transformSExp(e1), transformSExp(e2)) // helper
      // Begin
      case id("begin") :: es => Exp.Begin(transformBody(es))
      // Set_
      case List(id("set!"), id(name), body) => Set_(name, transformSExp(body))
      // Lam
      case List(id("lambda"), sexpr(args), body) =>
        val argsS = args.map({ case id(x) => x })
        Lam(argsS, transformBody(List(body)))
      // If
      case List(id("if"), e1, e2) => If(transformSExp(e1), transformSExp(e2), Nil_) // this is a hack
      case List(id("if"), e1, e2, e3) => If(transformSExp(e1), transformSExp(e2), transformSExp(e3))
      case List(id("cond"), sexpr(List(e1, e2)), sexpr(List(id("else"), e3))) => If(transformSExp(e1), transformSExp(e2), transformSExp(e3)) // this is a hack
      // Let
      case id("let") :: sexpr(args) :: body =>
        val argsE = args.map({ case sexpr(List(id(name), se)) => (name, transformSExp(se)) })
        Let(argsE, transformBody(body))
      case id("let*") :: sexpr(args) :: body =>
        val argsE = args.map({ case sexpr(List(id(name), se)) => (name, transformSExp(se)) })
        Let(List(argsE.head), argsE.tail.foldRight(transformBody(body)) { (x,y) => Body(Nil, List(Let(List(x), y))) })
      // LetRec
      case id("letrec") :: sexpr(args) :: body =>
        val argsE = args.map({ case sexpr(List(id(name), se)) => (name, transformSExp(se)) })
        LetRec(argsE, transformBody(body), false)
      // Op1, missing: random
      case List(id("number?"), e) => Op1(IsNumber, transformSExp(e))
      case List(id("integer?"), e) => Op1(IsInteger, transformSExp(e))
      case List(id("real?"), e) => Op1(IsDouble, transformSExp(e))
      case List(id("rational?"), e) => Op1(IsRational, transformSExp(e))
      case List(id("zero?"), e) => Op1(IsZero, transformSExp(e))
      case List(id("positive?"), e) => Op1(IsPositive, transformSExp(e))
      case List(id("negative?"), e) => Op1(IsNegative, transformSExp(e))
      case List(id("odd?"), e) => Op1(IsOdd, transformSExp(e))
      case List(id("even?"), e) => Op1(IsEven, transformSExp(e))
      case List(id("null?"), e) => Op1(IsNull, transformSExp(e))
      case List(id("cons?"), e) => Op1(IsCons, transformSExp(e))
      case List(id("pair?"), e) => Op1(IsCons, transformSExp(e))
      case List(id("boolean?"), e) => Op1(IsBoolean, transformSExp(e))
      case List(id("abs"), e) => Op1(Abs, transformSExp(e))
      case List(id("floor"), e) => Op1(Floor, transformSExp(e))
      case List(id("ceiling"), e) => Op1(Ceiling, transformSExp(e))
      case List(id("log"), e) => Op1(Log, transformSExp(e))
      case List(id("not"), e) => Op1(Not, transformSExp(e))
      case List(id("car"), e) => Op1(Car, transformSExp(e))
      case List(id("cdr"), e) => Op1(Cdr, transformSExp(e))
      case List(id("caar"), e) => Op1(Caar, transformSExp(e))
      case List(id("cadr"), e) => Op1(Cadr, transformSExp(e))
      case List(id("cddr"), e) => Op1(Cddr, transformSExp(e))
      case List(id("cadrr"), e) => Op1(Caddr, transformSExp(e))
      case List(id("cadddr"), e) => Op1(Cadddr, transformSExp(e))
      case List(id("number->string"), e) => Op1(NumberToString, transformSExp(e))
      case List(id("string->symbol"), e) => Op1(StringToSymbol, transformSExp(e))
      case List(id("symbol->string"), e) => Op1(SymbolToString, transformSExp(e))
      // Op2, missing StringRef
      case List(id("eq?"), e1, e2) => Op2(Eqv, transformSExp(e1), transformSExp(e2))
      case List(id("eqv?"), e1, e2) => Op2(Eqv, transformSExp(e1), transformSExp(e2))
      case List(id("quotient"), e1, e2) => Op2(Quotient, transformSExp(e1), transformSExp(e2))
      case List(id("remainder"), e1, e2) => Op2(Remainder, transformSExp(e1), transformSExp(e2))
      case List(id("modulo"), e1, e2) => Op2(Modulo, transformSExp(e1), transformSExp(e2))
      // OpVar
      case id("=") :: rest => OpVar(Equal, rest.map(transformSExp))
      case id("<") :: rest => OpVar(Smaller, rest.map(transformSExp))
      case id(">") :: rest => OpVar(Greater, rest.map(transformSExp))
      case id("<=") :: rest => OpVar(SmallerEqual, rest.map(transformSExp))
      case id(">=") :: rest => OpVar(GreaterEqual, rest.map(transformSExp))
      case id("max") :: rest => OpVar(Max, rest.map(transformSExp))
      case id("min") :: rest => OpVar(Min, rest.map(transformSExp))
      case id("+") :: rest => OpVar(Add, rest.map(transformSExp))
      case id("*") :: rest => OpVar(Mul, rest.map(transformSExp))
      case id("-") :: rest => OpVar(Sub, rest.map(transformSExp))
      case id("/") :: rest => OpVar(Div, rest.map(transformSExp))
      case id("gcd") :: rest => OpVar(Gcd, rest.map(transformSExp))
      case id("lcm") :: rest => OpVar(Lcm, rest.map(transformSExp))
      case id("string-append") :: rest => OpVar(StringAppend, rest.map(transformSExp))
      // helper start
      case id("or") :: x :: xs => If(Op2(Eqv, transformSExp(x), Lit(BoolLit(true))), Lit(BoolLit(true)), transformSExp(sexpr(id("or") :: xs)))
      case id("or") :: Nil => Lit(BoolLit(false))
      case id("and") :: x :: xs => If(Op2(Eqv, transformSExp(x), Lit(BoolLit(true))), transformSExp(sexpr(id("and") :: xs)), Lit(BoolLit(false)))
      case id("and") :: Nil => Lit(BoolLit(true))
      // helper stop
      // Error
      case List(id("error"), string(str)) => Error(str)
      // App
      case id(procVar) :: args => Apply(Var(procVar), args.map(transformSExp))
      case lam :: args => Apply(transformSExp(lam), args.map(transformSExp))
      case _ => throw new IllegalArgumentException(s"Not matched: $ses")
    case _ => transformBase(se)
