package sturdy.language.whilelang

import sturdy.effect.branching.BoolBranching
import sturdy.effect.environment.Environment
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.util.Label
import sturdy.values.booleans.BooleanOps
import sturdy.values.doubles.DoubleOps
import sturdy.values.doubles.DoubleOps
import Expr._
import Statement._
import sturdy.fix.Fixpoint
import sturdy.values.relational._

type GenericEffects[V, Addr] =
  BoolBranching[V] with
  Environment[String, Addr] with
  Store[Addr, V] with
  Allocation[Addr, Label] with
  Failure

case object UnboundVariable extends FailureKind
case object UnboundAddr extends FailureKind

trait GenericInterpreter[V, Addr, Effects <: GenericEffects[V, Addr], Fix <: Fixpoint[Statement, Unit]]
  (using val effectOps: Effects)
  (using val fixpoint: Fix)
  (using val boolOps: BooleanOps[V], doubleOps: DoubleOps[V], compareOps: CompareOps[V, V], eqOps: EqOps[V, V])
  (using effectOps.EnvJoin[V], effectOps.StoreJoin[V], effectOps.EnvJoin[Unit], effectOps.StoreJoin[Unit], effectOps.BoolBranchJoin[Unit]):

  import boolOps._
  import doubleOps._
  import compareOps._
  import eqOps._
  import effectOps._

  def eval(e: Expr): V = e match {
    case Var(x) =>
      lookupOrElseAndThen(x, fail(UnboundVariable, x)) { addr =>
        readOrElse(addr, fail(UnboundAddr, s"$addr for variable $x"))
      }
    case BoolLit(b) => boolLit(b)
    case And(e1, e2) => and(eval(e1), eval(e2))
    case Or(e1, e2) => or(eval(e1), eval(e2))
    case Not(e) => not(eval(e))
    case NumLit(n) => numLit(n)
    case Add(e1, e2) => add(eval(e1), eval(e2))
    case Sub(e1, e2) => sub(eval(e1), eval(e2))
    case Mul(e1, e2) => mul(eval(e1), eval(e2))
    case Div(e1, e2) => div(eval(e1), eval(e2))
    case Eq(e1, e2) => equ(eval(e1), eval(e2))
    case Lt(e1, e2) => lt(eval(e1), eval(e2))
    case RandomDouble() => randomDouble()
  }

  private val fixed: fixpoint.Fixed = fixpoint.fix { fixed =>
    inline def run(s: Statement): Unit = fixed(s)

    def run_open(s: Statement): Unit = s match
      case s@Assign(x, e) =>
        val v = eval(e)
        lookupOrElseAndThen(x, {
          val addr = alloc(s.label)
          bind(x, addr)
          addr
        }) { addr =>
          write(addr, v)
        }
      case If(cond, thn, els) =>
        boolBranch(eval(cond), run(thn), run(els))
      case s@While(cond, body) =>
        boolBranch(eval(cond), {run(body); run(s)}, {})
      case Block(body) =>
        body.foldLeft(())((_,s) => run(s))

    run_open(_)
  }

  def run = fixed