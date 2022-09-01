package sturdy.values

import apron.Texpr1Node
import sturdy.apron.Apron

given ApronPartialOrder(using ap: Apron): PartialOrder[Topped[Texpr1Node]] with
  override def lteq(x: Topped[Texpr1Node], y: Topped[Texpr1Node]): Boolean = (x,y) match
    case (Topped.Top,Topped.Top) => true
    case (Topped.Top, _) => false
    case (_, Topped.Top) => true
    case _ =>
      // TODO improve this, because getBound is over-appoximating
      ap.getBound(x).isLeq(ap.getBound(y))
