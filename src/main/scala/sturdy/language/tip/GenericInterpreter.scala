package sturdy.language.tip

import sturdy.effect.allocation.Allocation
import sturdy.effect.branching.BoolBranching
import sturdy.effect.environment.Environment
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.print.Print
import sturdy.effect.store.Store
import sturdy.effect.userinput.UserInput
import sturdy.util.Label
import sturdy.values.booleans.BooleanOps
import sturdy.values.ints.IntOps
import sturdy.values.functions.FunctionOps
import sturdy.values.relational.{EqOps, CompareOps}
import sturdy.fix
import sturdy.values.*
import sturdy.values.references.ReferenceOps

import scala.collection.mutable.ListBuffer

object GenericInterpreter:
  type GenericEffects[V, Addr] =
    BoolBranching[V] with
    Environment[String, Addr] with
    Store[Addr, V] with
    Allocation[Addr, AllocationSite] with
    Print[V] with
    UserInput[V] with
    Failure

  enum AllocationSite:
    case Alloc(e: Exp.Alloc)
    case ParamBinding(fun: Function, name: String)
    case LocalBinding(fun: Function, name: String)

  case object UnboundVariable extends FailureKind
  case object UnboundAddr extends FailureKind
  case object UserError extends FailureKind

  enum FixIn[V]:
    case Eval(e: Exp)
    case Run(s: Stm)
    case EnterFunction(f: Function)
  enum FixOut[V]:
    case Eval(v: V)
    case Run()
    case ExitFunction(ret: V)

  given joinFixOut[V](using j: JoinValue[V]): JoinValue[FixOut[V]] with
    override def joinValues(out1: FixOut[V], out2: FixOut[V]): FixOut[V] = (out1, out2) match
      case (FixOut.Eval(v1), FixOut.Eval(v2)) => FixOut.Eval(j.joinValues(v1, v2))
      case (FixOut.Run(), FixOut.Run()) => FixOut.Run()
      case (FixOut.ExitFunction(v1), FixOut.ExitFunction(v2)) => FixOut.ExitFunction(j.joinValues(v1, v2))
      case _ => throw new IllegalArgumentException(s"Cannot join outputs of different kind, $out1 and $out2")

  given finiteFixOut[V](using f: Finite[V]): Finite[FixOut[V]] with {}

  given widenFixOut[V](using w: fix.Widening[V]): fix.Widening[FixOut[V]] with
    override def widen(out1: FixOut[V], out2: FixOut[V]): FixOut[V] = (out1, out2) match
      case (FixOut.Eval(v1), FixOut.Eval(v2)) => FixOut.Eval(w.widen(v1, v2))
      case (FixOut.Run(), FixOut.Run()) => FixOut.Run()
      case (FixOut.ExitFunction(v1), FixOut.ExitFunction(v2)) => FixOut.ExitFunction(w.widen(v1, v2))
      case _ => throw new IllegalArgumentException(s"Cannot join outputs of different kind, $out1 and $out2")

  type GenericPhi[V] = fix.Combinator[FixIn[V], FixOut[V]]

import GenericInterpreter.*

trait GenericInterpreter[V, Addr, Effects <: GenericEffects[V, Addr]]
  (using val effectOps: Effects)
  (using intOps: IntOps[V], compareOps: CompareOps[V, V], eqOps: EqOps[V, V], functionOps: FunctionOps[Function, V, V, V], refOps: ReferenceOps[Addr, V])
  (using effectOps.EnvJoin[V], effectOps.StoreJoin[V], effectOps.EnvJoin[Unit], effectOps.StoreJoin[Unit], effectOps.BoolBranchJoin[Unit]):

  import intOps._
  import compareOps._
  import eqOps._
  import effectOps._
  import functionOps._
  import refOps._

  val phi: GenericPhi[V]

  private var functions: Map[String, Function] = Map()

  private lazy val fixed = fix.Fixpoint { (rec: FixIn[V] => FixOut[V]) =>
    def eval(e: Exp): V = rec(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
    def run(s: Stm): Unit = rec(FixIn.Run(s)) match {case FixOut.Run() => (); case v => throw new IllegalStateException()}

    def eval_open(e: Exp): V = e match {
      case Exp.NumLit(n) => intLit(n)
      case Exp.Input() => readInput()
      case Exp.Var(x) => functions.get(x) match
        case Some(fun) => funValue(fun)
        case None =>
          lookupOrElseAndThen(x, fail(UnboundVariable, x)) { addr =>
            readOrElse(addr, fail(UnboundAddr, s"$addr for variable $x"))
          }
      case Exp.Add(e1, e2) => add(eval(e1), eval(e2))
      case Exp.Sub(e1, e2) => sub(eval(e1), eval(e2))
      case Exp.Mul(e1, e2) => mul(eval(e1), eval(e2))
      case Exp.Div(e1, e2) => div(eval(e1), eval(e2))
      case Exp.Gt(e1, e2) => gt(eval(e1), eval(e2))
      case Exp.Eq(e1, e2) =>
        val v1 = eval(e1)
        val v2 = eval(e2)
        equ(v1, v2)
      case Exp.Call(fun, args) => invokeFun(eval(fun), args.map(eval))(call)
      case a@Exp.Alloc(e) =>
        val addr = alloc(AllocationSite.Alloc(a))
        write(addr, eval(e))
        refValue(addr)
      case Exp.VarRef(x) =>
        lookupOrElseAndThen(x, fail(UnboundVariable, x))(refValue)
      case Exp.Deref(e) =>
        val addr = refAddr(eval(e))
        readOrElse(addr, fail(UnboundAddr, addr.toString))
      case Exp.NullRef() =>
        nullValue
      case Exp.Record(fields) =>
        ???
      case Exp.FieldAccess(rec, field) =>
        ???
    }

    def run_open(s: Stm): Unit = s match
      case Stm.Assign(lhs: Assignable, e: Exp) =>
        val v = eval(e)
        assign(lhs, v)
      case Stm.If(cond: Exp, thn: Stm, els: Option[Stm]) =>
        boolBranch(eval(cond), run(thn), els.map(run).getOrElse(()))
      case Stm.While(cond, body) =>
        boolBranch(eval(cond), {run(body); run(s)}, {})
      case Stm.Block(body) =>
        body.foreach(run)
      case Stm.Output(e) =>
        print(eval(e))
      case Stm.Error(e) =>
        fail(UserError, eval(e).toString)

    def assign(lhs: Assignable, v: V): Unit = lhs match
      case Assignable.AVar(x) =>
        lookupOrElseAndThen(x, fail(UnboundVariable, x)) { addr =>
          write(addr, v)
        }
      case Assignable.ADeref(e) =>
        val addr = refAddr(eval(e))
        write(addr, v)
      case Assignable.AField(rec, field) =>
        ???
      case Assignable.ADerefField(rec, field) =>
        ???

    def call(fun: Function, args: Seq[V]): V = freshScoped {
      val localAddrs = ListBuffer[Addr]()
      fun.params.zip(args).map { case (name, arg) =>
        val addr = alloc(AllocationSite.ParamBinding(fun, name))
        bind(name, addr)
        write(addr, arg)
        localAddrs += addr
      }
      fun.locals.map { name =>
        val addr = alloc(AllocationSite.LocalBinding(fun, name))
        bind(name, addr)
        localAddrs += addr
      }
      try {
        rec(FixIn.EnterFunction(fun)) match
          case FixOut.ExitFunction(v) => v
          case _ => throw new IllegalStateException()
      } finally {
        localAddrs.foreach(free(_))
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
