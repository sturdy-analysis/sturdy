package sturdy.values.ordering

import apron.{Tcons1, Texpr1Node}

given ApronOrderingOps: OrderingOps[Texpr1Node, Tcons1] with
  override def lt(v1: Texpr1Node, v2: Texpr1Node): Tcons1 = ???
  override def le(v1: Texpr1Node, v2: Texpr1Node): Tcons1 = ???
