package sturdy.values.ordering

import apron.{Tcons1, Texpr1BinNode, Texpr1Node}
import sturdy.apron.Apron
import sturdy.values.Topped

given ApronOrderingOps(using ap: Apron): OrderingOps[Texpr1Node, Topped[Tcons1]] with
  override def lt(v1: Texpr1Node, v2: Texpr1Node): Topped[Tcons1] =
    // v1 < v2 iff v2 - v1 > 0
    ap.makeConstraint(Texpr1BinNode(Texpr1BinNode.OP_SUB, v2, v1), Tcons1.SUP)
  override def le(v1: Texpr1Node, v2: Texpr1Node): Topped[Tcons1] =
    // v1 =< v2 iff v2 - v1 >= 0
    ap.makeConstraint(Texpr1BinNode(Texpr1BinNode.OP_SUB, v2, v1), Tcons1.SUPEQ)

given ApronEqOps(using ap: Apron) : EqOps[Texpr1Node, Topped[Tcons1]] with
  override def equ(v1 : Texpr1Node, v2 : Texpr1Node) : Topped[Tcons1] =
    ap.makeConstraint(Texpr1BinNode(Texpr1BinNode.OP_SUB, v1, v2), Tcons1.EQ)
  override def neq(v1 : Texpr1Node, v2 : Texpr1Node) : Topped[Tcons1] =
    ap.negateExpr(equ(v1,v2))
