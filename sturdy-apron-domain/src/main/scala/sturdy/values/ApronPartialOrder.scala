package sturdy.values

import apron.Texpr1Node
import sturdy.apron.Apron

given ApronPartialIntOrder(using ap: Apron): PartialOrder[Texpr1Node] with
  override def lteq(x: Texpr1Node, y: Texpr1Node): Boolean =
    // TODO improve this, because getBound is over-appoximating
    ap.getBound(x).isLeq(ap.getBound(y))
