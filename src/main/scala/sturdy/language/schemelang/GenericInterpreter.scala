package sturdy.language.schemelang

import sturdy.effect.allocation.Allocation
import sturdy.effect.branching.BoolBranching
import sturdy.effect.environment.ClosableEnvironment
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.fix
import sturdy.language.schemelang.*
import Literal.*
import Expr.*
import Op1Kinds.*
import Op2Kinds.*
import OpVarKinds.*
import sturdy.util.Label
import sturdy.values.ints.{IntOps, IntDoubleOps, IntBoolOps}
import sturdy.values.rationals.{RationalOps, RationalIntOps, RationalDoubleOps, RationalBoolOps}
import sturdy.values.doubles.{DoubleOps, DoubleIntOps, DoubleBoolOps}
import sturdy.values.booleans.BooleanOps
import sturdy.values.chars.CharOps
import sturdy.values.strings.StringOps
import sturdy.values.closures.ClosureOps
import sturdy.values.relational.*

object GenericInterpreter:
  type GenericEffects[V, Addr, Env] =
    BoolBranching[V] with
    ClosableEnvironment[String, Addr, Env] with
    Store[Addr, V] with
    Allocation[Addr, AllocationSite] with
    Failure

  enum AllocationSite:
    case LetBinding(let: Expr, x: String)
    case Define(d: Expr.Define)
    case ClosureParam(body: List[Expr], x: String)

  case object UnboundVariable extends FailureKind
  case object UnboundAddr extends FailureKind
  case object UserError extends FailureKind
  case object IllegalArgument extends FailureKind

  enum FixIn[V]:
    case Eval(e: Expr)
    case Run(es: List[Expr])
  enum FixOut[V]:
    case Eval(v: V)
    case Run(v: V)

  type GenericPhi[V] = fix.Combinator[FixIn[V], FixOut[V]]

trait IfIsTypeOps[V]:
  def ifIsInt[A](v: V, thn: => A, els: => A): A
  def ifIsRatio[A](v: V, thn: => A, els: => A): A
  def ifIsDouble[A](v: V, thn: => A, els: => A): A

trait TypeOps[V]:
  def isNumber(v: V): V
  def isInteger(v: V): V
  def isDouble(v: V): V
  def isRational(v: V): V
  def isNull(v: V): V
  def isCons(v: V): V
  def isBoolean(v: V): V

trait VoidOps[V]:
  def void: V

trait SymbolOps[V]:
  def symbolLit(s: String): V

trait QuoteOps[L, V]:
  def quoteLit(l: L): V

import GenericInterpreter.*

trait GenericInterpreter[V, Addr, Env, Effects <: GenericEffects[V, Addr, Env]]
  (using val effectOps: Effects)
  (using val intOps: IntOps[V], intDoubleOps: IntDoubleOps[V, V], intBoolOps: IntBoolOps[V, V],
             rationalOps: RationalOps[V], rationalIntOps: RationalIntOps[V,V], rationalDoubleOps: RationalDoubleOps[V,V], rationalBoolOps: RationalBoolOps[V, V],
             doubleOps: DoubleOps[V], doubleIntOps: DoubleIntOps[V, V], doubleBoolOps: DoubleBoolOps[V, V],
             boolOps: BooleanOps[V],
             eqOps: EqOps[V, V], compareOps: CompareOps[V, V],
             charOps: CharOps[V], stringOps: StringOps[V],
             symbolOps: SymbolOps[V], quoteOps: QuoteOps[Literal, V],
             closureOps: ClosureOps[String, V, List[Expr], Env, V, V],
             voidOps: VoidOps[V], typeOps: TypeOps[V], ifIsTypeOps: IfIsTypeOps[V])
  (using effectOps.EnvJoin[V], effectOps.EnvJoin[Addr], effectOps.StoreJoin[V], effectOps.EnvJoin[Unit],
   effectOps.StoreJoin[Unit], effectOps.BoolBranchJoin[V]):

  import boolOps._
  import eqOps._
  import charOps._
  import stringOps._
  import symbolOps._
  import quoteOps._
  import effectOps._
  import voidOps._
  import closureOps._
  import compareOps._
  import typeOps._
  import ifIsTypeOps._

  val phi: GenericPhi[V]

  private lazy val fixed = fix.Fixpoint { (rec: FixIn[V] => FixOut[V]) =>
    def eval(e: Expr): V = rec(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
    def run(es: List[Expr]): V = rec(FixIn.Run(es)) match {case FixOut.Run(v) => v; case _ => throw new IllegalStateException()}

    def eval_open(e: Expr): V = { e match
      case Lit(l) => lit(l)
      case Nil_ => ???
      case Cons(e1, e2) => ???
      case Begin(es) => run(es)
      case AppFoo(e1, args) =>
        val clsVal = eval(e1)
        val argVals = args.map(arg => eval(arg))
        invokeClosure(clsVal, argVals) { applyClosure }
      case Apply(es) => run(es)
      case Var(x) =>
        lookupOrElseAndThen(x, fail(UnboundVariable, x)) {
          addr => readOrElse(addr, fail(UnboundAddr, s"$addr for variable $x"))
        }
      case e@Lam(names, body) => {
        val env = getEnv
        closureValue(names, body, env)
      }
      case expr@Let(bnds, body) =>
        scoped {
          bnds.foreach { case (x,e) =>
            val addr = alloc(AllocationSite.LetBinding(expr, x))
            val v = eval(e)
            bind(x, addr)
            write(addr, v)
          }
          run(body)
        }
      case expr@LetRec(bnds, body) =>
        val (envbnds, storebnds) = bnds.map { case (x,e) =>
          val addr = alloc(AllocationSite.LetBinding(expr, x))
          ((x,addr),(addr,e))
        }.unzip
        bindLocal_(envbnds) {
          storebnds.foreach { case (addr, e) =>
            val v = eval(e)
            write(addr, v)
          }
          run(body)
        }
      case s@Set_(x, e) =>
        val addr = lookupOrElse(x, fail(UnboundVariable, x))
        write(addr, eval(e))
        void
      case s@Define(x, e) => fail(UserError, "Define should only be used at top level")
      case If(e1, e2, e3) => boolBranch(eval(e1), eval(e2), eval(e3))
      case Op1(op, e) => op1(op, eval(e))
      case Op2(op, e1, e2) => op2(op, eval(e1), eval(e2))
      case OpVar(op, es) =>
        val vs = es.map(e => eval(e))
        opVar(op, vs)
      case Error(err) => fail(UserError, err)
    }

    def run_open(es: List[Expr]): V = { es match
      case (define@Define(x, e))::rest =>
        val addr = alloc(AllocationSite.Define(define))
        bindLocal(x, addr) {
          write(addr, eval(e))
          run(rest)
        }
      case Nil => void // ensures that empty program is evaluated
      case e::Nil => eval(e) // ensures that last evaluated value is returned
      case e::rest =>
        eval(e)
        run(rest)
    }

    def lit(literal: Literal): V = { literal match
      case IntLit(i) => intOps.intLit(i)
      case DoubleLit(d) => doubleOps.numLit(d)
      case RationalLit(i1, i2) => rationalOps.rationalLit(i1,i2)
      case BoolLit(b) => boolLit(b)
      case CharLit(c) => charLit(c)
      case StringLit(str) => stringLit(str)
      case SymbolLit(sym) => symbolLit(sym)
      case QuoteLit(qot) => quoteLit(qot)
    }

    def op1(op: Op1Kinds, v: V): V = { op match
      case IsNumber => isNumber(v)
      case IsInteger => isInteger(v)
      case IsDouble => isDouble(v)
      case IsRational => isRational(v)
      case IsZero => withNum1(v)(intBoolOps.isZero)(rationalBoolOps.isZero)(doubleBoolOps.isZero)
      case IsPositive => withNum1(v)(intBoolOps.isPositive)(rationalBoolOps.isZero)(doubleBoolOps.isPositive)
      case IsNegative => withNum1(v)(intBoolOps.isNegative)(rationalBoolOps.isZero)(doubleBoolOps.isNegative)
      case IsOdd => withInt1(v)(intBoolOps.isOdd)
      case IsEven => withInt1(v)(intBoolOps.isEven)
      case IsNull => isNull(v)
      case IsCons => isCons(v)
      case IsBoolean => isBoolean(v)

      case Abs => withNum1(v)(intOps.abs)(rationalOps.abs)(doubleOps.abs)
      case Floor => withNum1(v)(intOps.floor)(rationalIntOps.floor)(doubleIntOps.floor)
      case Ceiling => withNum1(v)(intOps.ceiling)(rationalIntOps.ceiling)(doubleIntOps.ceiling)
      case Log => withNum1(v)(intDoubleOps.log)(rationalDoubleOps.log)(doubleOps.log)

      case Not => not(v)

      case Car => ???
      case Cdr => ???
      case Caar => ???
      case Cadr => ???
      case Caddr => ???
      case Cadddr => ???

      case NumberToString => ???
      case StringToSymbol => ???
      case SymbolToString => ???

      case Random => ???
    }

    def op2(op: Op2Kinds, v1: V, v2: V): V = { op match
      case Eqv => ???

      case Quotient => withInt2(v1,v2)(intOps.quotient)
      case Remainder => withInt2(v1,v2)(intOps.remainder)
      case Modulo => withInt2(v1,v2)(intOps.modulo)

      case StringRef => ???
    }

    def opVar(op: OpVarKinds, vs: List[V]): V = { op match
      case Equal => ???
      case Smaller => vs.init.zip(vs.tail).map(lt).reduce(and)
      case Greater => vs.init.zip(vs.tail).map(gt).reduce(and)
      case SmallerEqual => vs.init.zip(vs.tail).map(ge).reduce(and)
      case GreaterEqual => vs.init.zip(vs.tail).map(le).reduce(and)
      case Max => vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.max)(rationalOps.max)(doubleOps.max) }
      case Min => vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.min)(rationalOps.min)(doubleOps.min) }
      case Add => vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.add)(rationalOps.add)(doubleOps.add) }
      case Mul => vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.mul)(rationalOps.mul)(doubleOps.mul) }
      case Sub => vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.sub)(rationalOps.sub)(doubleOps.sub) }
      case Div => vs.reduce { case (x1, x2) => withNum2(x1,x2)(intOps.div)(rationalOps.div)(doubleOps.div) }
      case Gcd => vs.reduce { case (x1, x2) => withInt2(x1,x2)(intOps.gcd) }
      case Lcm => vs.reduce { case (x1, x2) => withInt2(x1,x2)(intOps.lcm) }
      case StringAppend => ???
    }

    def applyClosure(vars: List[String], body: List[Expr], args: List[V], env: Env): V = {
      scoped {
        setEnv(env)
        val addrs = vars.map(x => alloc(AllocationSite.ClosureParam(body, x)))
        bindLocal_(vars.zip(addrs)) {
          addrs.zip(args).map((x,v) => write(x, v))
          run(body)
        }
      }
    }

    def withInt1(v1: V)(opInt: V => V): V =
      ifIsInt(v1, opInt(v1), fail(IllegalArgument, s"(withInt1): expected int but got $v1"))

    def withNum1(v1: V)(opInt: V => V)(opRatio: V => V)(opDouble: V => V): V = {
      ifIsInt(v1, opInt(v1),
        ifIsRatio(v1, opRatio(v1),
          ifIsDouble(v1, opDouble(v1),
            fail(IllegalArgument, s"(withNum1): expected num as argument but got $v1"))))
    }

    def withInt2(v1: V, v2: V)(opInt: (V, V) => V): V = {
      ifIsInt(v1,
        ifIsInt(v2,
          opInt(v1, v2),
          fail(IllegalArgument, s"(withInt2): expected int as second argument but got $v2")),
        fail(IllegalArgument, s"(withInt2): expected int as first argument but got $v1"))
    }

    def withNum2(v1: V, v2: V)(opInt: (V, V) => V)(opRatio: (V, V) => V)(opDouble: (V, V) => V): V = {
      ifIsInt(v1,
        ifIsInt(v2,
          opInt(v1,v2),
          ifIsRatio(v2,
            opRatio(v1, v2),
            ifIsDouble(v2,
              opDouble(v1,v2),
              fail(IllegalArgument, s"(withNum2):expected num as second argument but got $v2")))),
        ifIsRatio(v1,
          ifIsInt(v2,
            opRatio(v1, v2),
            ifIsRatio(v2,
              opRatio(v1,v2),
              ifIsDouble(v2,
                opDouble(v1,v2),
                fail(IllegalArgument, s"(withNum2):expected num as second argument but got $v2")))),
          ifIsDouble(v1,
            ifIsInt(v2,
              opDouble(v1,v2),
              ifIsRatio(v2,
                opDouble(v1,v2),
                ifIsDouble(v2,
                  opDouble(v1,v2),
                  fail(IllegalArgument, s"(withNum2): expected num as second argument but got $v2")))),
              fail(IllegalArgument, s"(withNum2): expected num as first argument but got $v1"))))
    }

    phi {
      case FixIn.Eval(e) => FixOut.Eval(eval_open(e))
      case FixIn.Run(es) => FixOut.Run(run_open(es))
    }
  }

  def eval(e: Expr): V = fixed(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
  def run(es: List[Expr]): V = fixed(FixIn.Run(es)) match {case FixOut.Run(v) => v; case _ => throw new IllegalStateException()}

