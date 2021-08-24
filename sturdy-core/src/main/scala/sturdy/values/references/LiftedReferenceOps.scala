package sturdy.values.references

import sturdy.effect.failure.Failure

class LiftedReferenceOps[V, Addr, UV](extract: V => UV, inject: UV => V)(using ops: ReferenceOps[Addr, UV])(using Failure) extends ReferenceOps[Addr, V]:
  def nullValue: V = inject(ops.nullValue)
  def refValue(addr: Addr): V = inject(ops.refValue(addr))
  def unmanagedRefValue(addr: Addr): V = inject(ops.unmanagedRefValue(addr))
  def refAddr(v: V): Addr = ops.refAddr(extract(v))
