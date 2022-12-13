package sturdy.language.tip

import sturdy.data.{MayJoin, noJoin}
import sturdy.effect.allocation.Allocation
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.environment.Environment
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.print.Print
import sturdy.effect.assert.Assert
import sturdy.effect.store.Store
import sturdy.effect.userinput.UserInput
import sturdy.util.Label
import sturdy.values.*
import sturdy.values.booleans.{BooleanBranching, BooleanOps}
import sturdy.values.integer.IntegerOps
import sturdy.values.functions.FunctionOps
import sturdy.values.records.RecordOps
import sturdy.values.ordering.{EqOps, OrderingOps}
import sturdy.fix
import sturdy.data.unit
import sturdy.effect.EffectStack
import sturdy.values.references.ReferenceOps

import scala.collection.mutable.ListBuffer

enum AllocationSite:
  case Alloc(e: Exp.Alloc)
  case ParamBinding(fun: Function, name: String)
  case LocalBinding(fun: Function, name: String)
  case Record(r: Exp.Record)

case class Field(name: String)
given Finite[Field] with {}

enum TipFailure extends FailureKind:
  case UnboundVariable
  case UnboundAddr
  case UserError
  case TypeError
  case VariableReferencesNotSupported
given Finite[TipFailure] with {}

enum FixIn:
  case Eval(e: Exp)
  case Run(s: Stm)
  case EnterFunction(f: Function)

  override def toString: String = this match
    case Eval(e) => s"eval $e"
    case Run(s) => s"run $s"
    case EnterFunction(fun) => s"enter ${fun.name}"

enum FixOut[V]:
  case Eval(v: V)
  case Run()
  case ExitFunction(ret: V)

given finiteFixIn: Finite[FixIn] with {}
//given finiteFixOut[V](using f: Finite[V]): Finite[FixOut[V]] with {}

given CombineFixOut[V, W <: Widening](using w: Combine[V, W]): Combine[FixOut[V], W] with
  override def apply(out1: FixOut[V], out2: FixOut[V]): MaybeChanged[FixOut[V]] = (out1, out2) match
    case (FixOut.Eval(v1), FixOut.Eval(v2)) => Combine[V, W](v1, v2).map(FixOut.Eval.apply)
    case (FixOut.Run(), FixOut.Run()) => Unchanged(FixOut.Run())
    case (FixOut.ExitFunction(v1), FixOut.ExitFunction(v2)) => Combine[V, W](v1, v2).map(FixOut.ExitFunction.apply)
    case _ => throw new IllegalArgumentException(s"Cannot combine outputs of different kind, $out1 and $out2")

import TipFailure.*

trait GenericInterpreter[V, Addr, J[_] <: MayJoin[_]] extends sturdy.Executor:

  // fixpoint
  val fixpoint: EffectStack ?=> fix.Fixpoint[FixIn, FixOut[V]]
  type Fixed = FixIn => FixOut[V]

  // joins
  implicit def jv: J[V]

  // value components
  val intOps: IntegerOps[Int, V]; import intOps.*
  val compareOps: OrderingOps[V, V]; import compareOps.*
  val eqOps: EqOps[V, V]; import eqOps.*
  val functionOps: FunctionOps[Function, Seq[V], V, V]; import functionOps.*
  val refOps: ReferenceOps[Addr, V]; import refOps.*
  val recOps: RecordOps[Field, V, V]; import recOps.*
  val branchOps: BooleanBranching[V, Unit]; import branchOps.*

  // effect components
  val callFrame: DecidableMutableCallFrame[String, String, V]
  val store: Store[Addr, V, J]
  val alloc: Allocation[Addr, AllocationSite]
  val print: Print[V]
  val assert: Assert[V]
  val input: UserInput[V]
  val failure: Failure

  // effect stack
  final val effectStack: EffectStack = new EffectStack(List(callFrame, store, alloc, print, assert, input, failure))
  given EffectStack = effectStack

  // analysis state
  protected var functions: Map[String, Function] = Map()
  def getFunctions: Iterable[Function] = functions.values

  def eval_open(e: Exp)(using Fixed): V = e match {
    case Exp.NumLit(n) => integerLit(n)
    case Exp.Input() => input.read()
    case Exp.Var(x) => functions.get(x) match
      case Some(fun) => funValue(fun)
      case None => callFrame.getLocalByName(x).getOrElse(failure(UnboundVariable, x))
    case Exp.Add(e1, e2) => add(eval(e1), eval(e2))
    case Exp.Sub(e1, e2) => sub(eval(e1), eval(e2))
    case Exp.Mul(e1, e2) => mul(eval(e1), eval(e2))
    case Exp.Div(e1, e2) => div(eval(e1), eval(e2))
    case Exp.Gt(e1, e2) => gt(eval(e1), eval(e2))
    case Exp.Eq(e1, e2) =>
      val v1 = eval(e1)
      val v2 = eval(e2)
      equ(v1, v2)
    case Exp.Call(fun, args) =>
      invokeFun(eval(fun), args.map(eval(_)))(call)
    case a@Exp.Alloc(e) =>
      val addr = alloc(AllocationSite.Alloc(a))
      store.write(addr, eval(e))
      refValue(addr)
    case Exp.VarRef(x) =>
      failure(VariableReferencesNotSupported, s"&$x")
//      val addr = callFrame.getLocalByName(x).getOrElse(failure(UnboundVariable, x))
//      unmanagedRefValue(addr)
    case Exp.Deref(e) =>
      val addr = refAddr(eval(e))
      val result = store.read(addr).getOrElse(failure(UnboundAddr, addr.toString))
      result
    case Exp.NullRef() =>
      nullValue
    case r@Exp.Record(fields) =>
      // represents record as a reference to a record value
      val fieldVals = fields.map(fe => Field(fe._1) -> eval(fe._2))
      val rec = makeRecord(fieldVals)
      val addr = alloc(AllocationSite.Record(r))
      store.write(addr, rec)
      refValue(addr)
    case Exp.FieldAccess(rec, field) =>
      val addr = refAddr(eval(rec))
      val recVal = store.read(addr).getOrElse(failure(UnboundAddr, addr.toString))
      lookupRecordField(recVal, Field(field))
  }

  def run_open(s: Stm)(using Fixed): Unit = s match
    case Stm.Assign(lhs: Assignable, e: Exp) =>
      val v = eval(e)
      assign(lhs, v)
    case Stm.If(cond: Exp, thn: Stm, els: Option[Stm]) =>
      boolBranch(eval(cond), run(thn), els.map(run(_)).getOrElse(()))
    case Stm.While(cond, body) =>
      boolBranch(eval(cond), {run(body); run(s)}, {})
    case Stm.Block(body) =>
      body.foreach(run(_))
    case Stm.Output(e) =>
      print(eval(e))
    case Stm.Assert(e) =>
      assert(eval(e))
    case Stm.Error(e) =>
      failure(UserError, eval(e).toString)

  def assign(lhs: Assignable, v: V)(using Fixed): Unit = lhs match
    case Assignable.AVar(x) =>
      callFrame.setLocalByName(x, v).getOrElse(failure(UnboundVariable, x))
    case Assignable.ADeref(e) =>
      val addr = refAddr(eval(e))
      store.write(addr, v)
    case Assignable.AField(recVar, field) =>
      val recRef = eval(Exp.Var(recVar))
      val recAddr = refAddr(recRef)
      val recVal = store.read(recAddr).getOrElse(failure(UnboundAddr, recAddr.toString))
      val updated = updateRecordField(recVal, Field(field), v)
      store.write(recAddr, updated)
    case Assignable.ADerefField(rec, field) =>
      val recRef = eval(rec)
      val recAddr = refAddr(recRef)
      val recVal = store.read(recAddr).getOrElse(failure(UnboundAddr, recAddr.toString))
      val updated = updateRecordField(recVal, Field(field), v)
      store.write(recAddr, updated)

  def call(fun: Function, args: Seq[V])(using Fixed): V =
    val locals: Iterable[(String, Option[V])] =
      fun.params.zip(args.map(Some.apply)) ++
      fun.locals.map(x => (x, None))
    callFrame.withNew(fun.name, locals) {
      enterFunction(fun)
    }

  inline def eval(e: Exp)(using rec: Fixed): V = rec(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
  inline def run(s: Stm)(using rec: Fixed): Unit = rec(FixIn.Run(s)) match {case FixOut.Run() => (); case _ => throw new IllegalStateException()}
  private inline def enterFunction(fun: Function)(using rec: Fixed) = rec(FixIn.EnterFunction(fun)) match {case FixOut.ExitFunction(v) => v; case _ => throw new IllegalStateException() }

  private lazy val fixed = {
    fixpoint {
      case FixIn.Eval(e) => FixOut.Eval(eval_open(e))
      case FixIn.Run(s) => run_open(s); FixOut.Run()
      case FixIn.EnterFunction(f) => FixOut.ExitFunction({run(f.body); eval(f.ret)})
    }
  }
  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  def execute(p: Program): V = external {
    functions = p.funs.map(f => f.name -> f).toMap
    val main = functions("main")
    val args = main.params.map(_ => Exp.Input())
    eval(Exp.Call(Exp.Var("main"), args))
  }
