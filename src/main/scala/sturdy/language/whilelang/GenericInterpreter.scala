package sturdy.language.whilelang

import sturdy.effect.environment.Environment
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.effect.failure.Failure
import sturdy.util.Label
import sturdy.values.booleans.BooleanOps
import sturdy.values.doubles.DoubleOps
import sturdy.values.doubles.DoubleOps
import Expr._
import Statement._
import sturdy.fix.Fixpoint
import sturdy.values.branch.BranchOps
import sturdy.values.relational._

type EffectfulOps[V, Addr] =
  Environment[String, Addr] with
  Store[Addr, V] with
  Allocation[Addr, Label] with
  Failure

trait GenericInterpreter[V, Addr, Effects <: EffectfulOps[V, Addr], Fix <: Fixpoint[Statement, Unit]]
  (using val effectOps: Effects)
  (using val fix: Fix)
  (using val boolOps: BooleanOps[V], doubleOps: DoubleOps[V], compareOps: CompareOps[V, V], eqOps: EqOps[V, V], branchOps: BranchOps[V])
  (using effectOps.EnvJoin[V], effectOps.StoreJoin[V], effectOps.EnvJoin[Unit], effectOps.StoreJoin[Unit], branchOps.BranchJoin[Unit]):

  import boolOps._
  import doubleOps._
  import compareOps._
  import eqOps._
  import branchOps._
  import effectOps._

  def eval(e: Expr): V = e match {
    case Var(x) =>
      lookupOrElseAndThen(x, fail(s"Unbound variable $x")) { addr =>
        readOrElse(addr, fail(s"Unbound address $addr for variable $x"))
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

  lazy val run: Statement => Unit = {
    fix.fix(rec => {
      case s@Assign(x, e) =>
        val v = eval(e)
        lookupOrElseAndThen(x, {
          val addr = alloc(s.label)
          bind(x, addr)
          addr
        }) { addr =>
          write(addr, v)
        }
      case If(cond, thn, els) => if_(eval(cond), rec(thn), rec(els))
      case s@While(cond, body) => rec(
        If(cond,
          Block(List(body, s)) <@@ s.label,
          Block(Nil) <@@ s.label)
          <@@ s.label)
      case Block(body) =>
        body.foldLeft(())((_,s) => rec(s))
    })
  }
