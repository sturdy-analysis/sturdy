//package sturdy.values.doubles
//
//import sturdy.values.JoinValue
//import sturdy.values.Topped
//
//given ToppedDoubleOps[V](using ops: DoubleOps[V]): DoubleOps[Topped[V]] with
//  def doubleLit(d: Double): Topped[V] = Actual(ops.doubleLit(d))
//  def randomDouble(): Topped[V] = Actual(ops.randomDouble())
//  def add(v1: Topped[V], v2: Topped[V]): Topped[V] =
//    for (d1 <- v1; d2 <- v2) yield ops.add(d1, d2)
//  def sub(v1: Topped[V], v2: Topped[V]): Topped[V] =
//    for (d1 <- v1; d2 <- v2) yield ops.sub(d1, d2)
//  def mul(v1: Topped[V], v2: Topped[V]): Topped[V] =
//    for (d1 <- v1; d2 <- v2) yield ops.mul(d1, d2)
//  def div(v1: Topped[V], v2: Topped[V]): Topped[V] =
//    for (d1 <- v1; d2 <- v2) yield ops.div(d1, d2)
