package sturdy.language.schemelang

import sturdy.util.Labeled

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


enum Expr extends Labeled:
  case Lit(l: Literal)
  case Nil_
  case Cons(e1: Expr, e2: Expr)
  case Begin(es: List[Expr])
  case AppFoo(foo: Expr, args: List[Expr])
  case Apply(body: List[Expr])
  case Var(name: String)
  case Set_(name: String, e: Expr)
  case Define(name: String, e: Expr)
  case Lam(names: List[String], body: List[Expr])
  case If(e1: Expr, e2: Expr, e3: Expr)
  case Let(bnds: List[(String, Expr)], body: List[Expr])
  case LetRec(bnds: List[(String, Expr)], body: List[Expr])
  case Op1(op: Op1Kinds, e: Expr)
  case Op2(op: Op2Kinds, e1: Expr, e2: Expr)
  case OpVar(op: OpVarKinds, es: List[Expr])
  case Error(s: String)
