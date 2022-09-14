package sturdy.values.ordering

import apron.{Tcons1, Texpr1BinNode}
import sturdy.apron.Apron
import sturdy.apron.ApronExpr
import sturdy.apron.BinOp
import sturdy.values.Topped

given ApronOrderingOps(using ap: Apron): OrderingOps[ApronExpr, Topped[Tcons1]] with
  override def lt(v1: ApronExpr, v2: ApronExpr): Topped[Tcons1] =
    // v1 < v2 iff v2 - v1 > 0
    Topped.Actual(ap.makeConstraint(ApronExpr.Binary(BinOp.Sub, v2, v1), Tcons1.SUP))
  override def le(v1: ApronExpr, v2: ApronExpr): Topped[Tcons1] =
    // v1 =< v2 iff v2 - v1 >= 0
    Topped.Actual(ap.makeConstraint(ApronExpr.Binary(BinOp.Sub, v2, v1), Tcons1.SUPEQ))

given ApronEqOps(using ap: Apron) : EqOps[ApronExpr, Topped[Tcons1]] with
  override def equ(v1 : ApronExpr, v2 : ApronExpr) : Topped[Tcons1] =
    Topped.Actual(ap.makeConstraint(ApronExpr.Binary(BinOp.Sub, v1, v2), Tcons1.EQ))
  override def neq(v1 : ApronExpr, v2 : ApronExpr) : Topped[Tcons1] =
    equ(v1,v2).map(ap.negateCons)
