package sturdy.values.objects

import sturdy.data.{JOption, MayJoin}
import sturdy.effect.store.Store

class LiftedObjectOps[Addr, Idx, OID, V, CF, O, OV, Site, Mth, J[_] <: MayJoin[_], UOV]
  (extractO: OV => UOV, injectO: UOV => OV)
  (using ops: ObjectOps[Addr, Idx, OID, V, CF, O, UOV, Site, Mth, J]) extends ObjectOps[Addr, Idx, OID, V, CF, O, OV, Site, Mth, J]:

  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V, Site)]): OV = injectO(ops.makeObject(oid, cfs, vals))
  override def getField(obj: OV, idx: Idx): JOption[J, V] = ops.getField(extractO(obj), idx)
  override def setField(obj: OV, idx: Idx, v: V): JOption[J, Unit] = ops.setField(extractO(obj), idx, v)
  override def invokeFunction(obj: OV, mth: Mth, args: Seq[V])(invoke: (O, Mth, Seq[V]) => V): V =
    ops.invokeFunction(extractO(obj), mth, args)(invoke)
