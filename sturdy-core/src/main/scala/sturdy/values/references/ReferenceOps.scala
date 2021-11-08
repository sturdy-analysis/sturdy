package sturdy.values.references

import sturdy.effect.Effectful
import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureKind
import sturdy.values.Powerset

case object NullDereference extends FailureKind

trait ReferenceOps[Addr, V] {
  def nullValue: V
  def refValue(addr: Addr): V
  def unmanagedRefValue(addr: Addr): V
  def refAddr(v: V): Addr
}

given ConcreteReferenceOps[Addr](using f: Failure): ReferenceOps[Addr, Option[Addr]] with
  def nullValue: Option[Addr] = None
  def refValue(addr: Addr): Option[Addr] = Some(addr)
  def unmanagedRefValue(addr: Addr): Option[Addr] = Some(addr)
  def refAddr(v: Option[Addr]): Addr = v.getOrElse(f.fail(NullDereference, ""))

given PowersetReferenceOps[Addr, V](using ops: ReferenceOps[Addr, V], j: Effectful): ReferenceOps[Powerset[Addr], Powerset[V]] with
  override def nullValue: Powerset[V] = Powerset(ops.nullValue)
  override def refValue(addr: Powerset[Addr]): Powerset[V] = addr.mapJoin(ops.refValue)
  override def unmanagedRefValue(addr: Powerset[Addr]): Powerset[V] = addr.mapJoin(ops.unmanagedRefValue)
  override def refAddr(v: Powerset[V]): Powerset[Addr] = v.mapJoin(ops.refAddr)
