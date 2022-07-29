package sturdy.values.booleans

import apron.MpqScalar
import apron.Tcons1
import apron.Texpr1CstNode
import apron.Texpr1UnNode
import sturdy.apron.Apron

given ApronBooleanOps(using ap: Apron): BooleanOps[Tcons1] with

  override def boolLit(b: Boolean): Tcons1 =
    val zeroIfTrue = if (b) 0 else 1
    ap.makeConstraint(new Texpr1CstNode(new MpqScalar(zeroIfTrue)), Tcons1.EQ)

  override def and(v1: Tcons1, v2: Tcons1): Tcons1 = ???

  override def or(v1: Tcons1, v2: Tcons1): Tcons1 = ???

  override def not(v: Tcons1): Tcons1 = {
    val exp = v.toTexpr1Node
    val negatedExp = new Texpr1UnNode(Texpr1UnNode.OP_NEG, exp)
    val notCond = ap.makeConstraint(negatedExp, v.getKind)
    notCond
  }