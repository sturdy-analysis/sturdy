package sturdy.values.references

import sturdy.effect.failure.Failure

class LiftedReferenceOps[V, Trg, UV](extract: V => UV, inject: UV => V)(using ops: ReferenceOps[Trg, UV]) extends ReferenceOps[Trg, V]:
  def mkNullRef: V = inject(ops.mkNullRef)
  def mkManagedRef(trg: Trg): V = inject(ops.mkManagedRef(trg))
  def mkRef(trg: Trg): V = inject(ops.mkRef(trg))
  def deref(v: V): Trg = ops.deref(extract(v))

