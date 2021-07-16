package sturdy.language.tip

import sturdy.effect.branching.BoolBranching
import sturdy.effect.environment.Environment
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.util.Label
import sturdy.values.booleans.BooleanOps
import sturdy.values.ints.IntOps
import sturdy.values.functions.FunctionOps
import sturdy.values.relational.{EqOps, CompareOps}
import sturdy.fix.Fixpoint
import sturdy.values.references.ReferenceOps

type GenericEffects[V, Addr] =
  BoolBranching[V] with
  Environment[String, Addr] with
  Store[Addr, V] with
  Allocation[Addr, Label] with
  Failure

case object UnboundVariable extends FailureKind
case object UnboundAddr extends FailureKind

trait GenericInterpreter[V, Addr, Effects <: GenericEffects[V, Addr], Fix <: Fixpoint[Either[Exp, Stm], Either[V, Unit]]]
  (using val effectOps: Effects)
  (using val fixpoint: Fix)
  (using val boolOps: BooleanOps[V], intOps: IntOps[V], compareOps: CompareOps[V, V], eqOps: EqOps[V, V], functionOps: FunctionOps[V, V], refOps: ReferenceOps[Addr, V])
  (using effectOps.EnvJoin[V], effectOps.StoreJoin[V], effectOps.EnvJoin[Unit], effectOps.StoreJoin[Unit], effectOps.BoolBranchJoin[Unit]):

  import boolOps._
  import intOps._
  import compareOps._
  import eqOps._
  import effectOps._
  import functionOps._
  import refOps._

  private val fixed: fixpoint.Fixed = fixpoint.fix { fixed =>
    inline def eval(e: Exp): V = fixed(Left(e)).swap.getOrElse(throw new IllegalStateException())
    inline def run(s: Stm): Unit = fixed(Right(s)).getOrElse(throw new IllegalStateException())

    def eval_open(e: Exp): V = e match {
      case Exp.Var(x) =>
        lookupOrElseAndThen(x, fail(UnboundVariable, x)) { addr =>
          readOrElse(addr, fail(UnboundAddr, s"$addr for variable $x"))
        }
      case Exp.NumLit(n) => intLit(n)
      case Exp.RandomInt() => randomInt()
      case Exp.Add(e1, e2) => add(eval(e1), eval(e2))
      case Exp.Sub(e1, e2) => sub(eval(e1), eval(e2))
      case Exp.Mul(e1, e2) => mul(eval(e1), eval(e2))
      case Exp.Div(e1, e2) => div(eval(e1), eval(e2))
      case Exp.Gt(e1, e2) => gt(eval(e1), eval(e2))
      case Exp.Eq(e1, e2) => equ(eval(e1), eval(e2))
      case Exp.Call(fun, args) => invoke(eval(fun), args.map(eval(_)))
      case Exp.Alloc(e) =>
        val addr = alloc(e.label)
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
      case Stm.Seq(s1: Stm, s2: Stm) =>
        run(s1)
        run(s2)
      case Stm.If(cond: Exp, thn: Stm, els: Option[Stm]) =>
        boolBranch(eval(cond), run(thn), els.map(run(_)).getOrElse(()))
      case Stm.While(cond, body) =>
        boolBranch(eval(cond), {run(body); run(s)}, {})

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

    dom => dom match {
      case Left(e) => Left(eval_open(e))
      case Right(s) => Right(run_open(s))
    }
  }

  def eval(e: Exp): V = fixed(Left(e)).swap.getOrElse(throw new IllegalStateException())
  def run(s: Stm): Unit = fixed(Right(s)).getOrElse(throw new IllegalStateException())


