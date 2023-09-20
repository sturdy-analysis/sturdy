package sturdy.values.references

import sturdy.effect.EffectStack
import sturdy.values.Powerset

given PowersetReferenceOps[Addr, V](using ops: ReferenceOps[Addr, V], j: EffectStack): ReferenceOps[Powerset[Addr], Powerset[V]] with
  override def nullValue: Powerset[V] = Powerset(ops.nullValue)
  override def refValue(addr: Powerset[Addr]): Powerset[V] = addr.mapJoin(ops.refValue)
  override def unmanagedRefValue(addr: Powerset[Addr]): Powerset[V] = addr.mapJoin(ops.unmanagedRefValue)
  override def refAddr(v: Powerset[V]): Powerset[Addr] = v.mapJoin(ops.refAddr)
