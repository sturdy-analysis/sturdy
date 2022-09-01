package sturdy.values.booleans

import apron.MpqScalar
import apron.Tcons1
import apron.Texpr1CstNode
import apron.Texpr1UnNode
import sturdy.apron.Apron
import sturdy.values.Topped

given ApronBooleanOps(using ap: Apron): BooleanOps[Topped[Tcons1]] with

  override def boolLit(b: Boolean): Topped[Tcons1] =
    val zeroIfTrue = if (b) 0 else 1
    ap.makeConstraint(new Texpr1CstNode(new MpqScalar(zeroIfTrue)), Tcons1.EQ)

  override def and(v1: Topped[Tcons1], v2: Topped[Tcons1]): Topped[Tcons1]= ???

  override def or(v1: Topped[Tcons1], v2: Topped[Tcons1]): Topped[Tcons1] = ???

  override def not(v: Topped[Tcons1]): Topped[Tcons1] = ap.negateExpr(v)
