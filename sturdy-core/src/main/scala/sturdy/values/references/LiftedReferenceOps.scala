package sturdy.values.references

class LiftedReferenceOps[V, Trg, UV](extract: V => UV, inject: UV => V)(using ops: ReferenceOps[Trg, UV]) extends ReferenceOps[Trg, V]:
  override def mkNullRef: V = inject(ops.mkNullRef)
  override def mkManagedRef(trg: Trg): V = inject(ops.mkManagedRef(trg))
  override def mkRef(trg: Trg): V = inject(ops.mkRef(trg))
  override def deref(v: V): Trg = ops.deref(extract(v))
