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
import sturdy.values.ints.IntOps
import sturdy.values.doubles.DoubleOps
import sturdy.values.booleans.BooleanOps
import sturdy.values.chars.CharOps
import sturdy.values.rational.RationalOps
import sturdy.values.strings.StringOps
import sturdy.values.symbols.SymbolOps
import sturdy.values.quotes.QuoteOps
import sturdy.values.void.VoidOps
import sturdy.values.types.TypeOps
import sturdy.values.closures.ClosureOps
import sturdy.values.relational.*
import sturdy.values.numerics.NumericOps

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

  enum FixIn[V]:
    case Eval(e: Expr)
    case Run(es: List[Expr])
  enum FixOut[V]:
    case Eval(v: V)
    case Run(v: V)

  type GenericPhi[V] = fix.Combinator[FixIn[V], FixOut[V]]

import GenericInterpreter.*

trait GenericInterpreter[V, Addr, Env, Effects <: GenericEffects[V, Addr, Env]]
  (using val effectOps: Effects)
  (using val intOps: IntOps[V], doubleOps: DoubleOps[V], numericOps: NumericOps[V],
             boolOps: BooleanOps[V],
             eqOps: EqOps[V, V], compareOps: CompareOps[V, V],
             charOps: CharOps[V], stringOps: StringOps[V],
             symbolOps: SymbolOps[V], quoteOps: QuoteOps[Literal, V],
             closureOps: ClosureOps[String, V, List[Expr], Env, V, V],
             voidOps: VoidOps[V], typeOps: TypeOps[V])
  (using effectOps.EnvJoin[V], effectOps.EnvJoin[Addr], effectOps.StoreJoin[V], effectOps.EnvJoin[Unit],
   effectOps.StoreJoin[Unit], effectOps.BoolBranchJoin[V]):

  import numericOps._
  import intOps.intLit
  import doubleOps.numLit
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

  val phi: GenericPhi[V]

  private lazy val fixed = fix.Fixpoint { (rec: FixIn[V] => FixOut[V]) =>
    def eval(e: Expr): V = rec(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
    def run(es: List[Expr]): V = rec(FixIn.Run(es)) match {case FixOut.Run(v) => v; case _ => throw new IllegalStateException()}

    def eval_open(e: Expr): V = e match {
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
        void()
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
      case Nil => void() // ensures that empty program is evaluated
      case e::Nil => eval(e) // ensures that last evaluated value is returned
      case e::rest =>
        eval(e)
        run(rest)
    }

    def lit(literal: Literal): V = literal match {
      case IntLit(i) => intLit(i)
      case DoubleLit(d) => numLit(d)
      case BoolLit(b) => boolLit(b)
      case CharLit(c) => charLit(c)
      case StringLit(str) => stringLit(str)
      case SymbolLit(sym) => symbolLit(sym)
      case QuoteLit(qot) => quoteLit(qot)
    }

    def op1(op: Op1Kinds, v: V): V = op match {
      case IsNumber => isNumber(v)
      case IsInteger => isInteger(v)
      case IsDouble => isDouble(v)
      case IsRational => isRational(v)
      case IsZero => isZero(v)
      case IsPositive => isPositive(v)
      case IsNegative => isNegative(v)
      case IsOdd => isOdd(v)
      case IsEven => isEven(v)
      case IsNull => isNull(v)
      case IsCons => isCons(v)
      case IsBoolean => isBoolean(v)

      case Abs => abs(v)
      case Floor => floor(v)
      case Ceiling => ceiling(v)
      case Log => log(v)

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

    def op2(op: Op2Kinds, v1: V, v2: V): V = op match {
      case Eqv => ???

      case Quotient => quotient(v1, v2)
      case Remainder => remainder(v1, v2)
      case Modulo => modulo(v1, v2)

      case StringRef => ???
    }

    def opVar(op: OpVarKinds, vs: List[V]): V = op match {
      case Equal => ???
      case Smaller => ??? // helpFoldBool(vs, lt) // TODO: fix, dont compare head with every element, but do pairwise compaarisons for consecutive elements
      case Greater => ??? // helpFoldBool(vs, gt) // TODO: fix, dont compare head with every element, but do pairwise compaarisons for consecutive elements
      case SmallerEqual => ??? // helpFoldBool(vs, le) // TODO: fix, dont compare head with every element, but do pairwise compaarisons for consecutive elements
      case GreaterEqual => ??? // helpFoldBool(vs, ge) // TODO: fix, dont compare head with every element, but do pairwise compaarisons for consecutive elements
      case Max => vs.reduce(max)
      case Min => vs.reduce(min)
      case Add => vs.reduce(add)
      case Mul => vs.reduce(mul)
      case Sub => vs.reduce(sub)
      case Div => vs.reduce(div)
      case Gcd => vs.reduce(gcd)
      case Lcm => vs.reduce(lcm)
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

//    def allocateBindings(bnds: List[(String, Expr)]): (List[(String, Addr)], List[(Addr, Expr)]) = {
//      val (vars, es) = bnds.unzip
//      val addrs = bnds.map((x,e) => alloc(AllocationSite.LetBinding(e,x)))
//      (vars.zip(addrs), addrs.zip(es))
//    }

//    def evalBindings(storebnds: List[(Addr, Expr)]): Unit = {
//      storebnds.map((addr, e) => { write(addr, run(List(e))) })
//    }

//    def helpFoldBool(vs: List[V], op: (V, V) => V): V = {
//      val bools = for v <- vs.tail yield op(vs.head, v)
//      bools.tail.fold(bools.head){and}
//    }
    phi {
      case FixIn.Eval(e) => FixOut.Eval(eval_open(e))
      case FixIn.Run(es) => FixOut.Run(run_open(es))
    }
  }

  def eval(e: Expr): V = fixed(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
  def run(es: List[Expr]): V = fixed(FixIn.Run(es)) match {case FixOut.Run(v) => v; case _ => throw new IllegalStateException()}

