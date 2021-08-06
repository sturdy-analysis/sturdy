package sturdy.values.doubles

import sturdy.values.JoinValue
import sturdy.values.Topped
import sturdy.values.Topped.*
import sun.nio.ch.ThreadPool

given ToppedDoubleOps[V](using ops: DoubleOps[V]): DoubleOps[Topped[V]] with
  def numLit(d: Double): Topped[V] = Actual(ops.numLit(d))
  def randomDouble(): Topped[V] = Actual(ops.randomDouble())
  def abs(v1: Topped[V]): Topped[V] = ???
  def log(v1: Topped[V]): Topped[V] = ???
  def add(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (d1 <- v1; d2 <- v2) yield ops.add(d1, d2)
  def sub(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (d1 <- v1; d2 <- v2) yield ops.sub(d1, d2)
  def mul(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (d1 <- v1; d2 <- v2) yield ops.mul(d1, d2)
  def div(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (d1 <- v1; d2 <- v2) yield ops.div(d1, d2)
  def max(v1: Topped[V], v2: Topped[V]): Topped[V] = ???
  def min(v1: Topped[V], v2: Topped[V]): Topped[V] = ???
