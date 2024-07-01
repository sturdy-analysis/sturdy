package sturdy.values.objects

import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.effect.store.Store

class LiftedObjectOps[FieldName, OID, V, CF, O, OV, Site, Mth, MthName, MthSig, NV, J[_] <: MayJoin[_], UOV, UNV]
  (extractO: OV => UOV, injectO: UOV => OV, extractNull: NV => UNV, injectNull: UNV => NV)
  (using ops: ObjectOps[FieldName, OID, V, CF, O, UOV, Site, Mth, MthName, MthSig, UNV, J]) extends ObjectOps[FieldName, OID, V, CF, O, OV, Site, Mth, MthName, MthSig, NV, J]:

  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V, Site, FieldName)]): OV = injectO(ops.makeObject(oid, cfs, vals))
  override def getField(obj: OV, name: FieldName): JOption[J, V] = ops.getField(extractO(obj), name)
  override def setField(obj: OV, name: FieldName, v: V): JOption[J, Unit] = ops.setField(extractO(obj), name, v)

  override def invokeFunctionCorrect(obj: OV, mthName: MthName, sig: MthSig, args: Seq[V])(invoke: (O, Mth, Seq[V]) => V): V =
    ops.invokeFunctionCorrect(extractO(obj), mthName, sig, args)(invoke)
  override def makeNull(): NV = injectNull(ops.makeNull())

