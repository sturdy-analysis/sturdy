//package sturdy.values.longs
//
//import sturdy.effect.failure.Failure
//import sturdy.values.JoinValue
//import sturdy.values.Topped
//
//given ToppedLongOps[V] (using ops: LongOps[V])(using Failure): LongOps[Topped[V]] with
//  def longLit(l: Long): Topped[V] = Actual(ops.longLit(l))
//  def randomLong(): Topped[V] = Actual(ops.randomLong())
//  def add(v1: Topped[V], v2: Topped[V]): Topped[V] =
//    for (d1 <- v1; d2 <- v2) yield ops.add(d1, d2)
//  def sub(v1: Topped[V], v2: Topped[V]): Topped[V] =
//    for (d1 <- v1; d2 <- v2) yield ops.sub(d1, d2)
//  def mul(v1: Topped[V], v2: Topped[V]): Topped[V] =
//    for (d1 <- v1; d2 <- v2) yield ops.mul(d1, d2)
//  def div(v1: Topped[V], v2: Topped[V]): Topped[V] =
//    for (d1 <- v1; d2 <- v2) yield ops.div(d1, d2)
