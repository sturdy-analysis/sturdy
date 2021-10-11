package sturdy.language.tip

import sturdy.effect.allocation.Allocation
import sturdy.effect.callframe.CCallFrame
import sturdy.effect.environment.Environment
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.print.Print
import sturdy.effect.store.Store
import sturdy.effect.userinput.UserInput
import sturdy.util.Label
import sturdy.values.*
import sturdy.values.booleans.{BooleanBranching, BooleanOps}
import sturdy.values.ints.IntOps
import sturdy.values.functions.FunctionOps
import sturdy.values.records.RecordOps
import sturdy.values.relational.{EqOps, CompareOps}
import sturdy.fix
import sturdy.data.unit
import sturdy.values.references.ReferenceOps

import scala.collection.mutable.ListBuffer

object GenericInterpreter:
  type GenericEffects[V, Addr, MayJoin[_]] =
    CCallFrame[Unit, String, Addr] with
    Store[Addr, V, MayJoin] with
    Allocation[Addr, AllocationSite] with
    Print[V] with
    UserInput[V] with
    Failure

  enum AllocationSite:
    case Alloc(e: Exp.Alloc)
    case ParamBinding(fun: Function, name: String)
    case LocalBinding(fun: Function, name: String)
    case Record(r: Exp.Record)

  case object UnboundVariable extends FailureKind
  case object UnboundAddr extends FailureKind
  case object UserError extends FailureKind
  case object TypeError extends FailureKind

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

  given finiteFixOut[V](using f: Finite[V]): Finite[FixOut[V]] with {}

  given CombineFixOut[V, W <: Widening](using w: Combine[V, W]): Combine[FixOut[V], W] with
    override def apply(out1: FixOut[V], out2: FixOut[V]): MaybeChanged[FixOut[V]] = (out1, out2) match
      case (FixOut.Eval(v1), FixOut.Eval(v2)) => Combine[V, W](v1, v2).map(FixOut.Eval.apply)
      case (FixOut.Run(), FixOut.Run()) => Unchanged(FixOut.Run())
      case (FixOut.ExitFunction(v1), FixOut.ExitFunction(v2)) => Combine[V, W](v1, v2).map(FixOut.ExitFunction.apply)
      case _ => throw new IllegalArgumentException(s"Cannot combine outputs of different kind, $out1 and $out2")

  type GenericPhi[V] = fix.Combinator[FixIn, FixOut[V]]

import GenericInterpreter.*

trait GenericInterpreter[V, Addr, MayJoin[_], Effects <: GenericEffects[V, Addr, MayJoin]]
  (val effects: Effects)
  (using MayJoin[Unit], MayJoin[V]):

  import effects.*

  val intOps: IntOps[V]; import intOps.*
  val compareOps: CompareOps[V, V]; import compareOps.*
  val eqOps: EqOps[V, V]; import eqOps.*
  val functionOps: FunctionOps[Function, V, V, V]; import functionOps.*
  val refOps: ReferenceOps[Addr, V]; import refOps.*
  val recOps: RecordOps[String, V, V]; import recOps.*
  val branchOps: BooleanBranching[V, MayJoin]; import branchOps.*

  val phi: GenericPhi[V]

  protected var functions: Map[String, Function] = Map()
  def getFunctions: Iterable[Function] = functions.values

  private lazy val fixed = fix.Fixpoint { (rec: FixIn => FixOut[V]) =>
    inline def eval(e: Exp): V = rec(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
    inline def run(s: Stm): Unit = rec(FixIn.Run(s)) match {case FixOut.Run() => (); case v => throw new IllegalStateException()}
    inline def enterFunction(fun: Function) = rec(FixIn.EnterFunction(fun)) match {case FixOut.ExitFunction(v) => v; case _ => throw new IllegalStateException() }

    def eval_open(e: Exp): V = e match {
      case Exp.NumLit(n) => intLit(n)
      case Exp.Input() => readInput()
      case Exp.Var(x) => functions.get(x) match
        case Some(fun) => funValue(fun)
        case None =>
          val addr = getLocal(x).getOrElse(fail(UnboundVariable, x))
          read(addr).getOrElse(fail(UnboundAddr, s"$addr for variable $x"))
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
        write(addr, eval(e))
        refValue(addr)
      case Exp.VarRef(x) =>
        val addr = getLocal(x).getOrElse(fail(UnboundVariable, x))
        unmanagedRefValue(addr)
      case Exp.Deref(e) =>
        val addr = refAddr(eval(e))
        read(addr).getOrElse(fail(UnboundAddr, addr.toString))
      case Exp.NullRef() =>
        nullValue
      case r@Exp.Record(fields) =>
        // represents record as a reference to a record value
        val fieldVals = fields.map(fe => fe._1 -> eval(fe._2))
        val rec = makeRecord(fieldVals)
        val addr = alloc(AllocationSite.Record(r))
        write(addr, rec)
        refValue(addr)
      case Exp.FieldAccess(rec, field) =>
        val recVal = eval(Exp.Deref(rec))
        lookupRecordField(recVal, field)
    }

    def run_open(s: Stm): Unit = s match
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
      case Stm.Error(e) =>
        fail(UserError, eval(e).toString)

    def assign(lhs: Assignable, v: V): Unit = lhs match
      case Assignable.AVar(x) =>
        val addr = getLocal(x).getOrElse(fail(UnboundVariable, x))
        write(addr, v)
      case Assignable.ADeref(e) =>
        val addr = refAddr(eval(e))
        write(addr, v)
      case Assignable.AField(recVar, field) =>
        val recRef = eval(Exp.Var(recVar))
        val recAddr = refAddr(recRef)
        val recVal = read(recAddr).getOrElse(fail(UnboundAddr, recAddr.toString))
        val updated = updateRecordField(recVal, field, v)
        write(recAddr, updated)
      case Assignable.ADerefField(rec, field) =>
        val recRef = eval(rec)
        val recAddr = refAddr(recRef)
        val recVal = read(recAddr).getOrElse(fail(UnboundAddr, recAddr.toString))
        val updated = updateRecordField(recVal, field, v)
        write(recAddr, updated)

    def call(fun: Function, args: Seq[V]): V =
      var locals: Map[String, Addr] = Map()
      val paramAddrs = fun.params.map { name =>
        val addr = alloc(AllocationSite.ParamBinding(fun, name))
        locals += name -> addr
        addr
      }
      val localsAddrs = fun.locals.map { name =>
        val addr = alloc(AllocationSite.LocalBinding(fun, name))
        locals += name -> addr
        addr
      }
      inNewFrame((), locals) {
        paramAddrs.zip(args).map(write)
        try enterFunction(fun)
        finally {
          paramAddrs.foreach(free)
          localsAddrs.foreach(free)
        }
      }

    phi {
      case FixIn.Eval(e) => FixOut.Eval(eval_open(e))
      case FixIn.Run(s) => {run_open(s); FixOut.Run()}
      case FixIn.EnterFunction(f) => FixOut.ExitFunction({run(f.body); eval(f.ret)})
    }
  }

  def eval(e: Exp): V = fixed(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
  def run(s: Stm): Unit = fixed(FixIn.Run(s)) match {case FixOut.Run() => (); case _ => throw new IllegalStateException()}

  def execute(p: Program): V =
    functions = p.funs.map(f => f.name -> f).toMap
    val main = functions("main")
    val args = main.params.map(_ => Exp.Input())
    eval(Exp.Call(Exp.Var("main"), args))
