package sturdy.values.ordering

import apron.Texpr1BinNode
import sturdy.apron.Apron
import sturdy.apron.ApronCons
import sturdy.apron.ApronExpr
import sturdy.apron.BinOp

given ApronOrderingOps: OrderingOps[ApronExpr, ApronCons] with
  override def lt(v1: ApronExpr, v2: ApronExpr): ApronCons = ApronCons.lt(v1, v2)
  override def le(v1: ApronExpr, v2: ApronExpr): ApronCons = ApronCons.le(v1, v2)
  override def ge(v1: ApronExpr, v2: ApronExpr): ApronCons = ApronCons.ge(v1, v2)
  override def gt(v1: ApronExpr, v2: ApronExpr): ApronCons = ApronCons.gt(v1, v2)

given ApronEqOps: EqOps[ApronExpr, ApronCons] with
  override def equ(v1 : ApronExpr, v2 : ApronExpr) : ApronCons = ApronCons.eq(v1, v2)
  override def neq(v1 : ApronExpr, v2 : ApronExpr) : ApronCons = ApronCons.neq(v1, v2)
