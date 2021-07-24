package sturdy.values.closure

import sturdy.effect.failure.Failure

import scala.util.Random

trait ClosureOps[Expr, Var, Addr, Val](using Failure):
  def closureToVal(cls: (Expr, Map[Var, Addr])): Val
  def valToClosure(v: Val): (Expr, Map[Var, Addr])
