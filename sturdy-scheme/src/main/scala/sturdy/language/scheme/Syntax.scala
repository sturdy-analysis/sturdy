package sturdy.language.scheme

import sturdy.util.Labled

enum Literal:
  case IntLit(i: Int)
  case DoubleLit(d: Double)
  case RationalLit(i1: Int, i2: Int)
  case BoolLit(b: Boolean)
  case CharLit(c: Char)
  case StringLit(s: String)
  case SymbolLit(s: String)
  case QuoteLit(l: Literal)

enum Op1Kinds:
  case IsNumber
  case IsInteger
  case IsDouble
  case IsRational
  case IsZero
  case IsPositive
  case IsNegative
  case IsOdd
  case IsEven
  case IsNull
  case IsCons
  case IsBoolean
  // Numeric Operations
  case Abs
  case Floor
  case Ceiling
  case Log
  // Boolean operations
  case Not
  // List operations
  case Car
  case Cdr
  case Caar
  case Cadr
  case Cddr
  case Caddr
  case Cadddr
  // String Operations
  case NumberToString
  case StringToSymbol
  case SymbolToString
  // Miscellaneous operations
  // case Error // do we need this?
  case Random


enum Op2Kinds:
  // Equivalence predicates
  case Eqv
  // Numerical operations
  case Quotient
  case Remainder
  case Modulo
  // String operations
  case StringRef


enum OpVarKinds:
  // Numerical operations
  case Equal
  case Smaller
  case Greater
  case SmallerEqual
  case GreaterEqual
  case Max
  case Min
  case Add
  case Mul
  case Sub
  case Div
  case Gcd
  case Lcm
  // String operations
  case StringAppend

case class Body(defs: List[Define], exps: List[Exp]):
  require(exps.nonEmpty)
object Body:
  def apply(exps: List[Exp]): Body = Body(List(), exps)
  def apply(e: Exp): Body = Body(List(), List(e))

enum Exp extends Labled:
  case Lit(l: Literal)
  case Nil_
  case Cons_(e1: Exp, e2: Exp)
  case Begin(es: List[Exp])
  case Apply(foo: Exp, args: List[Exp])
  case Var(name: String)
  case Set_(name: String, e: Exp)
  case Lam(names: List[String], body: Body)
  case If(e1: Exp, e2: Exp, e3: Exp)
  case Let(bnds: List[(String, Exp)], body: Body)
  case LetRec(bnds: List[(String, Exp)], body: Body, star: Boolean = true)
  case Op1(op: Op1Kinds, e: Exp)
  case Op2(op: Op2Kinds, e1: Exp, e2: Exp)
  case OpVar(op: OpVarKinds, es: List[Exp])
  case Error(s: String)

case class Define(name: String, e: Exp)

case class Program(forms: List[Form])
enum Form:
  case Expression(e: Exp)
  case Definition(d: Define)
  case Begin(fs: List[Form])