package sturdy.values.objects

import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.effect.store.Store

class LiftedObjectOps[FieldName, OID, V, CF, OV, Site, Mth, MthName, MthSig, B, J[_] <: MayJoin[_], UOV, UB]
  (extractO: OV => UOV, injectO: UOV => OV, extractB: B => UB, injectB: UB => B)
  (using ops: ObjectOps[FieldName, OID, V, CF, UOV, Site, Mth, MthName, MthSig, UB, J]) extends ObjectOps[FieldName, OID, V, CF, OV, Site, Mth, MthName, MthSig, B, J]:

  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V, Site, FieldName)]): OV = injectO(ops.makeObject(oid, cfs, vals))
  override def getField(obj: OV, name: FieldName): JOption[J, V] = ops.getField(extractO(obj), name)
  override def setField(obj: OV, name: FieldName, v: V): JOption[J, Unit] = ops.setField(extractO(obj), name, v)

  override def invokeFunctionCorrect(obj: OV, mthName: MthName, sig: MthSig, args: Seq[V])(invoke: (OV, Mth, Seq[V]) => V): V =
    def liftedInvoke = (uov: UOV, mth: Mth, vs: Seq[V]) => invoke(injectO(uov), mth, vs)
    ops.invokeFunctionCorrect(extractO(obj), mthName, sig, args)(liftedInvoke)

  override def makeNull(): OV = injectO(ops.makeNull())

  override def isNull(obj: OV): B = injectB(ops.isNull(extractO(obj)))


