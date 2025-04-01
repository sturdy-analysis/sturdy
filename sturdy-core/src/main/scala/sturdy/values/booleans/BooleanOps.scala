package sturdy.values.booleans

import sturdy.data.MayJoin
import sturdy.values.Structural

trait BooleanOps[V]:
  def boolLit(b: Boolean): V
  def and(v1: V, v2: V): V
  def not(v: V): V
  def or(v1: V, v2: V): V
  def imply(v1: V, v2: V): V = or(not(v1), v2)

given ConcreteBooleanOps: BooleanOps[Boolean] with
  def boolLit(b: Boolean): Boolean = b
  def and(v1: Boolean, v2: Boolean): Boolean = v1 && v2
  def not(v: Boolean): Boolean = !v
  def or(v1: Boolean, v2: Boolean): Boolean = v1 || v2

given IntBoolsBooleanOps[I,B](using ib: IntBools[I,B], ops: BooleanOps[B]): BooleanOps[I] =
  new LiftedBooleanOps(ib.intToBool, ib.boolToInt)

given Structural[Boolean] with {}
