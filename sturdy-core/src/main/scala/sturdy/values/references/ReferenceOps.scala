package sturdy.values.references

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureKind
import sturdy.values.Powerset

case object NullDereference extends FailureKind

trait ReferenceOps[Trg, V]:
  def mkNullRef: V
  def mkExternNullRef: V = mkNullRef
  def mkRef(trg: Trg): V
  def mkManagedRef(trg: Trg): V = mkRef(trg)
  def deref(v: V): Trg

given ConcreteReferenceOps[Trg] (using f: Failure): ReferenceOps[Trg, Option[Trg]] with
  override def mkNullRef: Option[Trg] = None
  override def mkRef(trg: Trg): Option[Trg] = Some(trg)
  override def mkManagedRef(trg: Trg): Option[Trg] = Some(trg)
  override def deref(v: Option[Trg]): Trg = v.getOrElse(f.fail(NullDereference, ""))


given PowersetReferenceOps[Trg, V](using ops: ReferenceOps[Trg, V], j: EffectStack): ReferenceOps[Powerset[Trg], Powerset[V]] with
  override def mkNullRef: Powerset[V] = Powerset(ops.mkNullRef)
  override def mkRef(trg: Powerset[Trg]): Powerset[V] = trg.mapJoin(ops.mkRef)
  override def mkManagedRef(trg: Powerset[Trg]): Powerset[V] = trg.mapJoin(ops.mkManagedRef)
  override def deref(v: Powerset[V]): Powerset[Trg] = v.mapJoin(ops.deref)
