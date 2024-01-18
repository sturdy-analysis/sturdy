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
import sturdy.language.tip.backward.values.*
import sturdy.util.Label
import sturdy.values.*
import sturdy.values.booleans.{BooleanBranching, BooleanOps}
import sturdy.values.functions.FunctionOps
import sturdy.values.integer.IntegerOps
import sturdy.values.records.RecordOps
import sturdy.values.references.ReferenceOps
import sturdy.values.relational.{EqOps, OrderingOps}

import scala.collection.mutable.ListBuffer

trait Meet[V]:
  def meet(v1: V, v2: V): Option[V]
  inline def apply(v1: V, v2: V): Option[V] = meet(v1, v2)
object Meet:
  def apply[V](v1: V, v2: V)(using meet: Meet[V]): Option[V] = meet(v1, v2)


enum TipBackFailure extends FailureKind:
  case BackwardsUnreachable
  case BackwardsUnboundVariable

enum BackFixIn[V]:
  case Eval(e: Exp, v: V)
  case Run(s: Stm)
  case Iterate(w: Stm.While)
  case EnterFunction(f: Function, ret: V)

  override def toString: String = this match
    case Eval(e, v) => s"evalBack $e = $v"
    case Run(s) => s"runBack $s"
    case Iterate(w) => s"iterateBack $w"
    case EnterFunction(fun, v) => s"enterBack ${fun.name} = $v"

enum BackFixOut[V]:
  case Eval(v: V)
  case Run()
  case Iterate()
  case ExitFunction(v: V)

given finiteBackFixIn[V]: Finite[BackFixIn[V]] with {}
//given finiteFixOut[V](using f: Finite[V]): Finite[FixOut[V]] with {}

given CombineBackFixOut[V, W <: Widening](using w: Combine[V, W]): Combine[BackFixOut[V], W] with
  override def apply(out1: BackFixOut[V], out2: BackFixOut[V]): MaybeChanged[BackFixOut[V]] = (out1, out2) match
    case (BackFixOut.Eval(v1), BackFixOut.Eval(v2)) => Combine[V, W](v1, v2).map(BackFixOut.Eval.apply)
    case (BackFixOut.Run(), BackFixOut.Run()) => Unchanged(BackFixOut.Run())
    case (BackFixOut.Iterate(), BackFixOut.Iterate()) => Unchanged(BackFixOut.Iterate())
    case (BackFixOut.ExitFunction(v1), BackFixOut.ExitFunction(v2)) => Combine[V, W](v1, v2).map(BackFixOut.ExitFunction.apply)
    case _ => throw new IllegalArgumentException(s"Cannot combine outputs of different kind, $out1 and $out2")

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

  val meet: Meet[V]

  // value components
  val intOps: BackIntegerOps[Int, V]; import intOps.*
  val compareOps: BackOrderingOps[V, V]; import compareOps.*
  val eqOps: EqOps[V, V]; import eqOps.*
  val backEqOps: BackEqOps[V, V]
  val functionOps: BackFunctionOps[Function, Seq[V], V, V]; import functionOps.*
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
  def topFunction: V

  // effect stack
  final val effectStack: EffectStack = new EffectStack(List(callFrame, store, alloc, print, input, failure))
  given EffectStack = effectStack

  // analysis state
  protected var functions: Map[String, Function] = Map()
  def getFunctions: Iterable[Function] = functions.values

  // assert(TopSign, Zero) should yield Zero
  // assert(Pos, Zero) should always fail
  def assert(v: V, expected: V): V =
    branchOps.boolBranch(eqOps.equ(v, expected)) {
      // fine
    } {
      failure(BackwardsUnreachable, s"not the asserted post value")
    }
    val m = meet(v, expected)
    m.getOrElse(failure(BackwardsUnreachable, s"empty meet"))

  def evalBack_open(e: Exp, expected: V)(using BackFixed): V = e match {
    case Exp.NumLit(n) => assert(integerLit(n), expected)
    case Exp.Input() => input.print(expected); expected
    case Exp.Var(x) => functions.get(x) match
      case Some(fun) => assert(funValue(fun), expected)
      case None =>
        val xVal = callFrame.getLocalByName(x).getOrElse(failure(BackwardsUnboundVariable, x))
        val refined = assert(xVal, expected)
        callFrame.setLocalByName(x, refined)
        refined
    case Exp.Add(e1, e2) => add(evalBack(e1,_), evalBack(e2,_), expected)
    case Exp.Sub(e1, e2) => sub(evalBack(e1,_), evalBack(e2,_), expected)
    case Exp.Mul(e1, e2) => mul(evalBack(e1,_), evalBack(e2,_), expected)
    case Exp.Div(e1, e2) => div(evalBack(e1,_), evalBack(e2,_), expected)
    case Exp.Gt(e1, e2) =>
      gt(evalBack(e1,_), evalBack(e2, _), expected)
    case Exp.Eq(e1, e2) => backEqOps.equ(evalBack(e1,_), evalBack(e2,_), expected)
    case Exp.Call(Exp.Var(f), args) =>
      val fun = functions.getOrElse(f, failure(UnboundVariable, s"Function $f"))
      val (argVals,v) = invokeFunBack(funValue(fun), expected)(callBack)
      args.zip(argVals).reverse.map(evalBack(_,_))
      v
    case _ => failure(BackwardsUnreachable, s"not implemented yet: expression $e")

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
    // TODO refinement?
    v

  def runBack_open(s: Stm)(using BackFixed): Unit = s match
    case s@Stm.Assign(lhs: Assignable, e: Exp) =>
      val v = assignBack(lhs, s)
      evalBack(e, v)
    case Stm.If(cond: Exp, thn: Stm, els: Option[Stm]) =>
      junit.eff.joinComputations {
        runBack(thn)
        evalBackNonzero(cond, topInt)
        ()
      } {
        els.foreach(runBack(_))
        evalBack(cond, integerLit(0))
        ()
      }
    case w@Stm.While(cond, body) =>
      evalBack(cond, integerLit(0))
      iterateBack(w)
    case Stm.Block(body) =>
      body.reverse.foreach(runBack(_))
    case Stm.Output(e) =>
      evalBack(e, print.read())
    case Stm.Error(e) =>
      failure(BackwardsUnreachable, s"Error $e")

  def iterateBack_open(w: Stm.While)(using BackFixed): Unit =
    junit.eff.joinComputations {
      // skip to predecessor
    } {
      runBack(w.body)
      evalBackNonzero(w.cond, topInt)
      iterateBack(w)
    }

  def assignBack(lhs: Assignable, s: Stm.Assign)(using BackFixed): V = lhs match
    case Assignable.AVar(x) =>
      val v = callFrame.getLocalByName(x).getOrElse(failure(BackwardsUnboundVariable, s.toString))
      callFrame.setLocalByName(x, topValue)
      v
    case _ => failure(BackwardsUnreachable, s"not implemented yet: assignable $lhs")
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

  def callBack(fun: Function, ret: V)(using BackFixed): (Seq[V], V) =
    val locals = fun.params ++ fun.locals
    val localsInit = locals.map(x => x -> topValue).toMap
    callFrame.withNew((), localsInit) {
      val v = enterFunction(fun, ret)
      val args = fun.params.map(callFrame.getLocalByName.andThen(_.get))
      (args, v)
    }

  inline def evalBack(e: Exp, v: V)(using rec: BackFixed): V = rec(BackFixIn.Eval(e, v)) match {case BackFixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
  inline def runBack(s: Stm)(using rec:  BackFixed): Unit = rec(BackFixIn.Run(s)) match {case BackFixOut.Run() => (); case _ => throw new IllegalStateException()}
  inline def iterateBack(w: Stm.While)(using rec: BackFixed): Unit = rec(BackFixIn.Iterate(w)) match {case BackFixOut.Iterate() => (); case _ => throw new IllegalStateException()}

  private inline def enterFunction(fun: Function, ret: V)(using rec: BackFixed) = rec(BackFixIn.EnterFunction(fun, ret)) match {case BackFixOut.ExitFunction(v) => v; case _ => throw new IllegalStateException() }

  private lazy val fixedBack = {
    fixpoint {
      case BackFixIn.Eval(e, v) => BackFixOut.Eval(evalBack_open(e, v))
      case BackFixIn.Run(s) => runBack_open(s); BackFixOut.Run()
      case BackFixIn.Iterate(w) => iterateBack_open(w); BackFixOut.Iterate()
      case BackFixIn.EnterFunction(f, ret) => BackFixOut.ExitFunction({
        val v = evalBack(f.ret, ret)
        runBack(f.body)
        v
      })
    }
  }
  inline def external[A](f: BackFixed ?=> A): A = f(using fixedBack)

  def executeBack(p: Program, expected: V): (Seq[V], V) = external {
    functions = p.funs.map(f => f.name -> f).toMap
    val main = functions("main")
    callBack(main, expected)
  }
