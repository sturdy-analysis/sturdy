package sturdy.values.ints

import sturdy.effect.failure.Failure
import sturdy.values.JoinValue
import sturdy.values.Topped
import sturdy.values.Topped.*

given ToppedIntOps[V] (using ops: IntOps[V])(using Failure): IntOps[Topped[V]] with
  def intLit(i: Int): Topped[V] = Actual(ops.intLit(i))
  def randomInt(): Topped[V] = Actual(ops.randomInt())
  def add(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (d1 <- v1; d2 <- v2) yield ops.add(d1, d2)
  def sub(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (d1 <- v1; d2 <- v2) yield ops.sub(d1, d2)
  def mul(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (d1 <- v1; d2 <- v2) yield ops.mul(d1, d2)
  def div(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (d1 <- v1; d2 <- v2) yield ops.div(d1, d2)
  def abs(i: Topped[V]): Topped[V] = ???
  def ceiling(i: Topped[V]): Topped[V] = ???
  def floor(i: Topped[V]): Topped[V] = ???
  def gcd(v1: Topped[V], v2: Topped[V]): Topped[V] = ???
  def lcm(v1: Topped[V], v2: Topped[V]): Topped[V] = ???
  def max(v1: Topped[V], v2: Topped[V]): Topped[V] = ???
  def min(v1: Topped[V], v2: Topped[V]): Topped[V] = ???
  def modulo(v1: Topped[V], v2: Topped[V]): Topped[V] = ???
  def quotient(v1: Topped[V], v2: Topped[V]): Topped[V] = ???
  def remainder(v1: Topped[V], v2: Topped[V]): Topped[V] = ???