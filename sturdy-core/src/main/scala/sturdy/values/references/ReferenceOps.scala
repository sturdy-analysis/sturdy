package sturdy.values.references

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureKind
import sturdy.values.{Finite, Powerset, Structural}

case object NullDereference extends FailureKind

trait ReferenceOps[Addr, V] {
  def nullValue: V
  def refValue(addr: Addr): V
  def unmanagedRefValue(addr: Addr): V
  def refAddr(v: V): Addr
}
