package sturdy.language.schemelang

import sturdy.effect.allocation.Allocation
import sturdy.effect.branching.BoolBranching
import sturdy.effect.closure.Closure
import sturdy.effect.environment.Environment
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
import sturdy.values.closure.ClosureOps
import sturdy.values.relational.*

object GenericInterpreter:
  type GenericEffects[V, Addr] =
    BoolBranching[V] with
    Environment[String, Addr] with
    Store[Addr, V] with
    Allocation[Addr, AllocationSite] with
    Failure

  enum AllocationSite:
    case LetBinding(arg: Expr, x: String)
    case Define(d: Expr.Define)
    case ClosureArg(lam: Expr, x: String)

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

trait GenericInterpreter[V, Addr , Effects <: GenericEffects[V, Addr]]
  (using val effectOps: Effects)
  (using val intOps: IntOps[V],
             boolOps: BooleanOps[V],
             eqOps: EqOps[V, V], compareOps: CompareOps[V, V],
             charOps: CharOps[V], stringOps: StringOps[V],
             symbolOps: SymbolOps[V], quoteOps: QuoteOps[Literal, V],
             voidOps: VoidOps[V], typeOps: TypeOps[V]) //, closureOps: ClosureOps[Expr, String, Addr, V])
  (using effectOps.EnvJoin[V], effectOps.EnvJoin[Addr], effectOps.StoreJoin[V], effectOps.EnvJoin[Unit],
   effectOps.StoreJoin[Unit], effectOps.BoolBranchJoin[V]):

  import intOps._
  import boolOps._
  import eqOps._
  import charOps._
  import stringOps._
  import symbolOps._
  import quoteOps._
  import effectOps._
  import voidOps._
//  import closureOps._
  import compareOps._

  val phi: GenericPhi[V]

  private lazy val fixed = fix.Fixpoint { (rec: FixIn[V] => FixOut[V]) =>
    def eval(e: Expr): V = rec(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
    def run(es: List[Expr]): V = rec(FixIn.Run(es)) match {case FixOut.Run(v) => v; case _ => throw new IllegalStateException()}


    def eval_open(e: Expr): V = e match {
      case Lit(l) => lit(l)
      case Nil_ => ???
      case Cons(e1, e2) => ???
      case Begin(es) => run(es)
//      case AppFoo(e1, args) =>
//        val foo = _run(List(e1))
//        val args = for e <- args yield (_run(List(e)),e) // Todo use map
//        apply(applyClosure, valToClosure(foo), (args,_run))
      case Apply(es) => run(es)
      case Var(x) => {
        lookupOrElseAndThen(x, fail(UnboundVariable, x)) {
        addr => readOrElse(addr, fail(UnboundAddr, s"$addr for variable $x"))
        }
      }
//      case e@Lam(names, body) => closureToVal(closure(e))
      case expr@Let(bnds, body) => {
//        val (vars, es) = bnds.unzip
//        val addrs = vars.map(x => alloc(AllocationSite.LetBinding(expr,x)))
//        val envbnds = vars.zip(addrs)
//        val storebnds = addrs.zip(es)
        val (envbnds, storebnds) = allocateBindings(bnds)
        evalBindings(storebnds)
        bindLocal_(envbnds) { run(body) }
      }
      case expr@LetRec(bnds, body) => {
//        val (vars, es) = bnds.unzip
//        val addrs = vars.map(x => alloc(AllocationSite.LetBinding(expr,x)))
//        val envbnds =  vars.zip(addrs)
//        val storebnds = addrs.zip(es)
        val (envbnds, storebnds) = allocateBindings(bnds)
        bindLocal_(envbnds) {
          evalBindings(storebnds)
          run(body)
        }
      }
        //      val addr = lookupOrElse(x, fail(s"(set!): cannot set variable $x before its definition"))
        //      val v = _run(List(e))
        //      write(addr, v)
        //      _run(rest)
        //    }
        //    case (s@Define(x, e)) :: rest => {
        //      val addr = alloc(s.label)
        //      bindLocal(x, addr) {
        //        val v = _run(List(e))
        //        write(addr, v)
        //        _run(rest)
        //      }
      case s@Set_(x, e) => {
        val addr = lookupOrElse(x, fail(UnboundVariable, x))
        write(addr, eval(e))
        void()
      }
      case s@Define(x, e) => fail(UserError, "Define should only be used at top level")
      case If(e1, e2, e3) => {
        val v = run(List(e1))
        boolBranch(v, run(List(e2)), run(List(e3)))
      }
      case Op1(op, e) => op1(op, run(List(e)))
      case Op2(op, e1, e2) => op2(op, run(List(e1)), run(List(e2)))
      case OpVar(op, es) => {
        val vs = for e <- es yield run(List(e))
        opVar(op, vs)
      }
      case Error(err) => fail(UserError, err)
    }

    def run_open(es: List[Expr]): V = { es match
      case (define@Define(x, e))::rest => {
        val addr = alloc(AllocationSite.Define(define))
        bindLocal(x, addr) {
          write(addr, eval(e))
          run(rest)
        }
      }
      case e::List() => { eval(e) } // ensures that last evaluated value is returned
      case List() => void() // ensures that empty program is evaluated
      case e::rest => {
        eval(e)
        run(rest)
      }
    }


    def lit(literal: Literal): V = literal match {
      case IntLit(i) => intLit(i)
      case BoolLit(b) => boolLit(b)
      case CharLit(c) => charLit(c)
      case StringLit(str) => stringLit(str)
      case SymbolLit(sym) => symbolLit(sym)
      case QuoteLit(qot) => quoteLit(qot)
    }

    def op1(op: Op1Kinds, v: V): V = op match {
      case IsNumber => typeOps.isNumber(v)
      case IsInteger => typeOps.isInteger(v)
      case IsDouble => typeOps.isDouble(v)
      case IsRational => typeOps.isRational(v)
      case IsZero => isZero(v)
      case IsPositive => isPositive(v)
      case IsNegative => isNegative(v)
      case IsOdd => isOdd(v)
      case IsEven => isEven(v)
      case IsNull => typeOps.isNull(v)
      case IsCons => typeOps.isCons(v)
      case IsBoolean => typeOps.isBoolean(v)

      case Abs => abs(v)
      case Floor => floor(v)
      case Ceiling => ceiling(v)

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

    // TODO
    def op2(op: Op2Kinds, v1: V, v2: V): V = op match {
      case Eqv => ???

      case Quotient => quotient(v1, v2)
      case Remainder => remainder(v1, v2)
      case Modulo => modulo(v1, v2)

      case StringRef => ???
    }


    // TODO
    def opVar(op: OpVarKinds, vs: List[V]): V = op match {
      case Equal => ???
      case Smaller => ??? // helpFoldBool(vs, lt) // TODO: fix, dont compare head with every element, but do pairwise compaarisons for consecutive elements
      case Greater => ??? // helpFoldBool(vs, gt) // TODO: fix, dont compare head with every element, but do pairwise compaarisons for consecutive elements
      case SmallerEqual => ??? // helpFoldBool(vs, le) // TODO: fix, dont compare head with every element, but do pairwise compaarisons for consecutive elements
      case GreaterEqual => ??? // helpFoldBool(vs, ge) // TODO: fix, dont compare head with every element, but do pairwise compaarisons for consecutive elements
      case Max => vs.tail.fold(vs.head){max}
      case Min => vs.tail.fold(vs.head){min}
      case Add => vs.tail.fold(vs.head){add}
      case Mul => vs.tail.fold(vs.head){mul}
      case Sub => vs.tail.fold(vs.head){sub}
      case Div => vs.tail.fold(vs.head){div}
      case Gcd => vs.tail.fold(vs.head){gcd}
      case Lcm => vs.tail.fold(vs.head){lcm}
      case StringAppend => ???
    }
//    def applyClosure(expr: Expr, args: (List[(V, Expr)], (List[Expr] => V))): V = {
//      expr match {
//        case Lam(xs, body) =>
//          if (xs.length == args._1.length) {
//            //TODO: fix address allocation to take variable names into account, or just pass labels/exprs
//            val addrs = for arg <- args._1 yield alloc(arg._2.label)
//            for addr <- addrs; arg <- args._1 yield write(addr, arg._1) // Todo zip addrs.zip(args...)
//            val bnds = for x <- xs; addr <- addrs yield (x, addr)
//            bindLocal_(bnds) {
//              args._2(List(Apply(body)))
//            }
//          } else fail (s"(applyClosure): applied function $expr with %i arguments to %i  arguments".format(xs.length, args._1.length))
//        case _ => fail(s"(applyClosure): expected a function, but got $expr")
//      }
//    }

//    def evalBindings(bnds: List[(String, Expr)]): List[(String, Addr)] =  {
//      for (x, e) <- bnds yield {
//        val v = run(List(e))
//        val addr = alloc(AllocationSite.LetBinding(e)
//        write(addr, v)
//        (x, addr)
//      }
//    }

    def allocateBindings(bnds: List[(String, Expr)]): (List[(String, Addr)], List[(Addr, Expr)]) = {
      val (vars, es) = bnds.unzip
      val addrs = bnds.map((x,e) => alloc(AllocationSite.LetBinding(e,x)))
      (vars.zip(addrs), addrs.zip(es))
    }

    def evalBindings(storebnds: List[(Addr, Expr)]): Unit = {
      storebnds.map((addr, e) => { write(addr, run(List(e))) })
    }

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




  // TODO
//  def eval(_run: List[Expr] => V): (Expr => V) = {
//    case Lit(l) => lit(l)
//    case Nil_ => ???
//    case Cons(e1, e2) => ???
//    case Begin(es) => _run(es)
//    case AppFoo(e1, args) =>
//      val foo = _run(List(e1))
//      val args = for e <- args yield (_run(List(e)),e) // Todo use map
//      apply(applyClosure, valToClosure(foo), (args,_run))
//    case Apply(es) => _run(es)
//    case Var(x) => {
//      lookupOrElseAndThen(x, fail(s"(var): variable $x does not exist in environment")) {
//        addr => readOrElse(addr, fail(s"(var): address $addr does not exist in store "))
//      }
//    }
//    case e@Lam(names, body) => closureToVal(closure(e))
//    case Let(bnds, body) => {
//      val vs = evalBindings(_run, bnds)
//      bindLocal_(vs){ _run(body) }
//    }
//    case LetRec(bnds, body) => {
//      val addrs = for (_, e) <- bnds yield alloc(e.label)
//      val envbnds =  for (x, _) <- bnds; addr <- addrs yield (x, addr)
//      val storebnds = for (_, e) <- bnds; addr <- addrs yield (addr, e)
//      bindLocal_(envbnds){ evalBindings_(_run, storebnds, body) }
//    }
//    case s@Set_(x, e) => _run(List(s))
//    case s@Define(x, e) => _run(List(s))
//    case If(e1, e2, e3) => {
//      val v = _run(List(e1))
//      boolBranch(v, _run(List(e2)), _run(List(e3)))
//    }
//    case Op1(op, e) => op1(op, _run(List(e)))
//    case Op2(op, e1, e2) => op2(op, _run(List(e1)), _run(List(e2)))
//    case OpVar(op, es) => {
//      val vs = for e <- es yield _run(List(e))
//      opVar(op, vs)
//    }
//    case Error(s) => fail(s"error: $s")
//  }

//  def run(_eval: Expr => V, _run: List[Expr] => V): (List[Expr] => V) = {
//    case Set_(x, e) :: rest => {
//      val addr = lookupOrElse(x, fail(s"(set!): cannot set variable $x before its definition"))
//      val v = _run(List(e))
//      write(addr, v)
//      _run(rest)
//    }
//    case (s@Define(x, e)) :: rest => {
//      val addr = alloc(s.label)
//      bindLocal(x, addr) {
//        val v = _run(List(e))
//        write(addr, v)
//        _run(rest)
//      }
//    }
//    case e::List() => _eval(e) // TODO remove
//    case List() => void()
//    case e::rest => {
//      _eval(e)
//      _run(rest)
//    }
//  }
//
//  lazy val runFixed: List[Expr] => V = {
//    fix.fix(_run => run( eval(_run), _run))
//  }

//  def applyClosure(expr: Expr, args: (List[(V, Expr)], (List[Expr] => V))): V = {
//    expr match {
//      case Lam(xs, body) =>
//        if (xs.length == args._1.length) {
//          //TODO: fix address allocation to take variable names into account, or just pass labels/exprs
//          val addrs = for arg <- args._1 yield alloc(arg._2.label)
//          for addr <- addrs; arg <- args._1 yield write(addr, arg._1) // Todo zip addrs.zip(args...)
//          val bnds = for x <- xs; addr <- addrs yield (x, addr)
//          bindLocal_(bnds) {
//            args._2(List(Apply(body)))
//          }
//        } else fail (s"(applyClosure): applied function $expr with %i arguments to %i  arguments".format(xs.length, args._1.length))
//      case _ => fail(s"(applyClosure): expected a function, but got $expr")
//    }
//  }
//
//  def evalBindings(_run: List[Expr] => V, bnds: List[(String, Expr)]): List[(String, Addr)] =  {
//    for (x, e) <- bnds yield {
//      val v = _run(List(e))
//      val addr = alloc(e.label)
//      write(addr, v)
//      (x, addr)
//    }
//  }
//
//  def evalBindings_(_run: List[Expr] => V, bnds: List[(Addr, Expr)], body: List[Expr]): V = {
//    for (addr, e) <- bnds yield {
//      val v = _run(List(e))
//      write(addr, v)
//    }
//    _run(body)
//  }
//
//  def helpFoldBool(vs: List[V], op: (V, V) => V): V = {
//    val bools = for v <- vs.tail yield op(vs.head, v)
//    bools.tail.fold(bools.head){and}
//  }

