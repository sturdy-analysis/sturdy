package sturdy.values

import apron.Texpr1Node
import sturdy.apron.Apron
import sturdy.apron.ApronExpr

given ApronPartialOrder(using ap: Apron): PartialOrder[ApronExpr] with
  override def lteq(x: ApronExpr, y: ApronExpr): Boolean =
    ap.getBound(x).isLeq(ap.getBound(y))
