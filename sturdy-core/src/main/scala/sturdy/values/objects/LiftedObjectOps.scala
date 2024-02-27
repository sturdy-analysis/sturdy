package sturdy.values.objects

import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.effect.store.Store

class LiftedObjectOps[Addr, Idx, OID, V, CF, O, OV, Site, Mth, MthName, MthSig, NV, TypeRep, J[_] <: MayJoin[_], UOV, UNV]
  (extractO: OV => UOV, injectO: UOV => OV, extractNull: NV => UNV, injectNull: UNV => NV)
  (using ops: ObjectOps[Addr, Idx, OID, V, CF, O, UOV, Site, Mth, MthName, MthSig, UNV, TypeRep, J]) extends ObjectOps[Addr, Idx, OID, V, CF, O, OV, Site, Mth, MthName, MthSig, NV, TypeRep, J]:

  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V, Site)]): OV = injectO(ops.makeObject(oid, cfs, vals))
  override def getField(obj: OV, idx: Idx): JOption[J, V] = ops.getField(extractO(obj), idx)
  override def setField(obj: OV, idx: Idx, v: V): JOption[J, Unit] = ops.setField(extractO(obj), idx, v)
  override def invokeFunction(obj: OV, mth: Mth, args: Seq[V])(invoke: (O, Mth, Seq[V]) => JOptionC[V]): JOptionC[V] =
    ops.invokeFunction(extractO(obj), mth, args)(invoke)
  override def findFunction(obj: OV, name: MthName, sig: MthSig)(invoke: (O, MthName, MthSig) => Mth): Mth =
    ops.findFunction(extractO(obj), name, sig)(invoke)
  override def makeNull(): NV = injectNull(ops.makeNull())
  override def isNull(nullVal: NV): Boolean =
    try {
      ops.isNull(extractNull(nullVal))
    }
    catch {
      case _ =>
        false
    }
  override def checkType(obj: OV, check: TypeRep)(checkFun: (O, TypeRep) => Boolean): Boolean =
    ops.checkType(extractO(obj), check)(checkFun)