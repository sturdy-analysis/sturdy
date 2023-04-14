package sturdy.values.booleans

import sturdy.data.MayJoin
import sturdy.values.{Combine, DiscretelyOrdered, Structural, Widening}

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

given DiscreteOrderedBoolean[W <: Widening]: Combine[Boolean, W] = DiscretelyOrdered[Boolean, W]

