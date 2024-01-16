package sturdy.language.tip.backward

import sturdy.data.MayJoin.WithJoin
import sturdy.data.{CombineUnit, MayJoin, noJoin, unit}
import sturdy.effect.EffectStack
import sturdy.effect.allocation.Allocation
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.environment.Environment
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.print.Print
import sturdy.effect.store.Store
import sturdy.effect.userinput.UserInput
import sturdy.fix
import sturdy.language.tip.TipFailure.UnboundVariable
import sturdy.language.tip.backward.TipBackFailure.*
import sturdy.language.tip.*
import sturdy.util.Label
import sturdy.values.*
import sturdy.values.booleans.{BooleanBranching, BooleanOps}
import sturdy.values.functions.FunctionOps
import sturdy.values.integer.IntegerOps
import sturdy.values.records.RecordOps
import sturdy.values.references.ReferenceOps
import sturdy.values.relational.{EqOps, OrderingOps}

import scala.collection.mutable.ListBuffer

enum TipBackFailure extends FailureKind:
  case BackwardsUnreachable
  case BackwardsUnboundVariable

enum BackFixIn[V]:
  case Eval(e: Exp, v: V)
  case Run(s: Stm)
  case EnterFunction(f: Function)

  override def toString: String = this match
    case Eval(e, v) => s"evalBack $e = $v"
    case Run(s) => s"runBack $s"
    case EnterFunction(fun) => s"enterBack ${fun.name}"

enum BackFixOut[V]:
  case Eval(v: V)
  case Run()
  case ExitFunction(ret: V)

given finiteBackFixIn[V]: Finite[BackFixIn[V]] with {}
//given finiteFixOut[V](using f: Finite[V]): Finite[FixOut[V]] with {}

given CombineBackFixOut[V, W <: Widening](using w: Combine[V, W]): Combine[BackFixOut[V], W] with
  override def apply(out1: BackFixOut[V], out2: BackFixOut[V]): MaybeChanged[BackFixOut[V]] = (out1, out2) match
    case (BackFixOut.Eval(v1), BackFixOut.Eval(v2)) => Combine[V, W](v1, v2).map(BackFixOut.Eval.apply)
    case (BackFixOut.Run(), BackFixOut.Run()) => Unchanged(BackFixOut.Run())
    case (BackFixOut.ExitFunction(v1), BackFixOut.ExitFunction(v2)) => Combine[V, W](v1, v2).map(BackFixOut.ExitFunction.apply)
    case _ => throw new IllegalArgumentException(s"Cannot combine outputs of different kind, $out1 and $out2")


trait BackIntegerOps[B, V]:
  def integerLit(i: B): V
  def randomInteger(): V

  def add(v1: V => V, v2: V => V, r: V): V
  def sub(v1: V => V, v2: V => V, r: V): V
  def mul(v1: V => V, v2: V => V, r: V): V
  def div(v1: V => V, v2: V => V, r: V): V

trait BackOrderingOps[V, B]:
  def lt(v1: V => V, v2: V => V, r: B): B
  def le(v1: V => V, v2: V => V, r: B): B

  def ge(v1: V => V, v2: V => V, r: B): B = le(v2, v1, r)
  def gt(v1: V => V, v2: V => V, r: B): B = lt(v2, v1, r)

trait BackEqOps[V, B]:
  def equ(v1: V => V, v2: V => V, r: B): B
  def neq(v1: V => V, v2: V => V, r: B): B


/**
 * The generic interpreter for the Tip language (https://github.com/cs-au-dk/TIP).
 *
 * The generic interpreter captures the core semantics of the language and
 * is used to derive abstract interpreters and as well as the concrete interpreter.
 * This is useful for sharing code between different abstract interpreters,
 * but also simplifies the soundness proof as no reasoning about the generic interpreter
 * is necessary (https://doi.org/10.1145/3236767).
 * @param V The type of values
 * @param Addr The type of addresses
 * @param J Abstracts over if the interpreter joins or not.
 *           - The concrete interpreter defines `J` as [[NoJoin]], meaning the interpreter does not join.
 *           - The abstract interpreters defines `J` to be [[WithJoin]], meaning the interpreter does join.
 */
trait GenericBackwardsInterpreter[V, Addr] extends sturdy.Executor:

  // fixpoint
  val fixpoint: EffectStack ?=> fix.Fixpoint[BackFixIn[V], BackFixOut[V]]
  type BackFixed = BackFixIn[V] => BackFixOut[V]

  implicit def jv: WithJoin[V]
  implicit def junit: WithJoin[Unit] = WithJoin(implicitly, jv.eff)

  // value components
  val intOps: BackIntegerOps[Int, V]; import intOps.*
  val compareOps: BackOrderingOps[V, V]; import compareOps.*
  val eqOps: EqOps[V, V]; import eqOps.*
  val backEqOps: BackEqOps[V, V]
  val functionOps: FunctionOps[Function, Seq[V], V, V]; import functionOps.*
  val refOps: ReferenceOps[Addr, V]; import refOps.*
  val recOps: RecordOps[Field, V, V]; import recOps.*
  val branchOps: BooleanBranching[V, Unit]; import branchOps.*

  // effect components
  val callFrame: DecidableMutableCallFrame[Unit, String, V]
  val store: Store[Addr, V, WithJoin]
  val alloc: Allocation[Addr, AllocationSite]
  val print: UserInput[V]
  val input: Print[V]
  val failure: Failure

  val topValue: V
  val topInt: V

  // effect stack
  final val effectStack: EffectStack = new EffectStack(List(callFrame, store, alloc, print, input, failure))
  given EffectStack = effectStack

  // analysis state
  protected var functions: Map[String, Function] = Map()
  def getFunctions: Iterable[Function] = functions.values

  def assert(v: V, expected: V): V =
    branchOps.boolBranch(eqOps.equ(v, expected)) {
      // fine
    } {
      failure(BackwardsUnreachable, s"not the asserted post value")
    }
    v

  def evalBack_open(e: Exp, expected: V)(using BackFixed): V = e match {
    case Exp.NumLit(n) => assert(integerLit(n), expected)
    case Exp.Input() => input.print(expected); expected
    case Exp.Var(x) => functions.get(x) match
      case Some(fun) => assert(funValue(fun), expected)
      case None =>
        val xVal = callFrame.getLocalByName(x).getOrElse(failure(UnboundVariable, x))
        assert(xVal, expected)
    case Exp.Add(e1, e2) => add(evalBack(e1,_), evalBack(e2,_), expected)
    case Exp.Sub(e1, e2) => sub(evalBack(e1,_), evalBack(e2,_), expected)
    case Exp.Mul(e1, e2) => mul(evalBack(e1,_), evalBack(e2,_), expected)
    case Exp.Div(e1, e2) => div(evalBack(e1,_), evalBack(e2,_), expected)
    case Exp.Gt(e1, e2) => gt(evalBack(e1, _), evalBack(e2, _), expected)
    case Exp.Eq(e1, e2) => backEqOps.equ(evalBack(e1,_), evalBack(e2,_), expected)
//    case Exp.Call(fun, args) =>
//      invokeFun(eval(fun), args.map(eval(_)))(call)
//    case a@Exp.Alloc(e) =>
//      val addr = alloc(AllocationSite.Alloc(a))
//      store.write(addr, eval(e))
//      refValue(addr)
//    case Exp.VarRef(x) =>
//      failure(VariableReferencesNotSupported, s"&$x")
//    //      val addr = callFrame.getLocalByName(x).getOrElse(failure(UnboundVariable, x))
//    //      unmanagedRefValue(addr)
//    case Exp.Deref(e) =>
//      val addr = refAddr(eval(e))
//      val result = store.read(addr).getOrElse(failure(UnboundAddr, addr.toString))
//      result
//    case Exp.NullRef() =>
//      nullValue
//    case r@Exp.Record(fields) =>
//      // represents record as a reference to a record value
//      val fieldVals = fields.map(fe => Field(fe._1) -> eval(fe._2))
//      val rec = makeRecord(fieldVals)
//      val addr = alloc(AllocationSite.Record(r))
//      store.write(addr, rec)
//      refValue(addr)
//    case Exp.FieldAccess(rec, field) =>
//      val addr = refAddr(eval(rec))
//      val recVal = store.read(addr).getOrElse(failure(UnboundAddr, addr.toString))
//      lookupRecordField(recVal, Field(field))
  }

  def evalBackNonzero(e: Exp, expected: V)(using BackFixed): V =
    val v = evalBack(e, expected)
    // then block can only run backwards if condition was != 0
    branchOps.boolBranch(eqOps.equ(v, integerLit(0))) {
      failure(BackwardsUnreachable, s"pre-condition $v == 0")
    } {
    }
    v

  def runBack_open(s: Stm)(using BackFixed): Unit = s match
    case s@Stm.Assign(lhs: Assignable, e: Exp) =>
      val v = assignBack(lhs, s)
      evalBack(e, v)
    case Stm.If(cond: Exp, thn: Stm, els: Option[Stm]) => els match
      case None =>
        runBack(thn)
        evalBackNonzero(cond, topInt)
      case Some(elsStm) =>
        junit.eff.joinComputations {
          runBack(thn)
          evalBackNonzero(cond, topInt)
          ()
        } {
          runBack(elsStm)
          evalBack(cond, integerLit(0))
          ()
        }
    case Stm.While(cond, body) =>
      evalBack(cond, integerLit(0))
      junit.eff.joinComputations {
        // skip to predecessor
      } {
        runBack(body)
        evalBackNonzero(cond, topInt)
        runBack(s)
      }
    case Stm.Block(body) =>
      body.reverse.foreach(runBack(_))
    case Stm.Output(e) =>
      evalBack(e, print.read())
    case Stm.Error(e) =>
      failure(BackwardsUnreachable, s"Error $e")

  def assignBack(lhs: Assignable, s: Stm.Assign)(using BackFixed): V = lhs match
    case Assignable.AVar(x) =>
      val v = callFrame.getLocalByName(x).getOrElse(failure(BackwardsUnboundVariable, s.toString))
      callFrame.setLocalByName(x, topValue)
      v
//    case Assignable.ADeref(e) =>
//      val addr = refAddr(eval(e))
//      store.write(addr, v)
//    case Assignable.AField(recVar, field) =>
//      val recRef = eval(Exp.Var(recVar))
//      val recAddr = refAddr(recRef)
//      val recVal = store.read(recAddr).getOrElse(failure(UnboundAddr, recAddr.toString))
//      val updated = updateRecordField(recVal, Field(field), v)
//      store.write(recAddr, updated)
//    case Assignable.ADerefField(rec, field) =>
//      val recRef = eval(rec)
//      val recAddr = refAddr(recRef)
//      val recVal = store.read(recAddr).getOrElse(failure(UnboundAddr, recAddr.toString))
//      val updated = updateRecordField(recVal, Field(field), v)
//      store.write(recAddr, updated)

//  def call(fun: Function, args: Seq[V])(using Fixed): V =
//    val locals: Map[String, V] =
//      Map() ++
//        fun.params.zip(args) ++
//        fun.locals.map(x => (x, integerLit(-1)))
//    callFrame.withNew((), locals) {
//      enterFunction(fun)
//    }

  inline def evalBack(e: Exp, v: V)(using rec: BackFixed): V = rec(BackFixIn.Eval(e, v)) match {case BackFixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
  inline def runBack(s: Stm)(using rec:  BackFixed): Unit = rec(BackFixIn.Run(s)) match {case BackFixOut.Run() => (); case _ => throw new IllegalStateException()}

//  private inline def enterFunction(fun: Function)(using rec: BackFixed) = rec(FixIn.EnterFunction(fun)) match {case FixOut.ExitFunction(v) => v; case _ => throw new IllegalStateException() }

  private lazy val fixedBack = {
    fixpoint {
      case BackFixIn.Eval(e, v) => BackFixOut.Eval(evalBack_open(e, v))
      case BackFixIn.Run(s) => runBack_open(s); BackFixOut.Run()
//      case BackFixIn.EnterFunction(f) => BackFixOut.ExitFunction({run(f.body); eval(f.ret)})
    }
  }
  inline def external[A](f: BackFixed ?=> A): A = f(using fixedBack)

  def executeBack(p: Program): Unit = external {
    functions = p.funs.map(f => f.name -> f).toMap
    val main = functions("main")
    runBack(main.body)
//    val args = main.params.map(_ => Exp.Input())
//    eval(Exp.Call(Exp.Var("main"), args))
  }
