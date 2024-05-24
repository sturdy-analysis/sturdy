package sturdy.values.objects

import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.effect.store.Store

class LiftedObjectOps[FieldName, OID, V, CF, O, OV, Site, Mth, MthName, MthSig, NV, J[_] <: MayJoin[_], UOV, UNV]
  (extractO: OV => UOV, injectO: UOV => OV, extractNull: NV => UNV, injectNull: UNV => NV)
  (using ops: ObjectOps[FieldName, OID, V, CF, O, UOV, Site, Mth, MthName, MthSig, UNV, J]) extends ObjectOps[FieldName, OID, V, CF, O, OV, Site, Mth, MthName, MthSig, NV, J]:

  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V, Site, FieldName)]): OV = injectO(ops.makeObject(oid, cfs, vals))
  override def getField(obj: OV, name: FieldName): JOption[J, V] = ops.getField(extractO(obj), name)
  override def setField(obj: OV, name: FieldName, v: V): JOption[J, Unit] = ops.setField(extractO(obj), name, v)
  override def invokeFunction(obj: OV, mth: Mth, args: Seq[V])(invoke: (O, Mth, Seq[V]) => JOptionC[V]): JOptionC[V] =
    ops.invokeFunction(extractO(obj), mth, args)(invoke)
  override def findFunction(obj: OV, name: MthName, sig: MthSig)(find: (O, MthName, MthSig) => Mth): Mth =
    ops.findFunction(extractO(obj), name, sig)(find)
  override def makeNull(): NV = injectNull(ops.makeNull())
  //override def isNull(nullVal: NV): Boolean =
  //  ops.isNull(extractNull(nullVal))

