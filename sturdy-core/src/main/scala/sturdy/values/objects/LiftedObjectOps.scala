package sturdy.values.objects

import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.effect.failure.Failure
import sturdy.effect.store.Store

class LiftedObjectOps[FieldName, OID, V, CF, OV, Site, Mth, MthName, MthSig, B, CallData, J[_] <: MayJoin[_], UOV, UB]
  (extractO: OV => UOV, injectO: UOV => OV, extractB: B => UB, injectB: UB => B)
  (using ops: ObjectOps[FieldName, OID, V, CF, UOV, Site, Mth, MthName, MthSig, UB, CallData, J]) extends ObjectOps[FieldName, OID, V, CF, OV, Site, Mth, MthName, MthSig, B, CallData, J]:

  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V, Site, FieldName)]): OV = injectO(ops.makeObject(oid, cfs, vals))
  override def getField(obj: OV, name: FieldName)(using Failure): V = ops.getField(extractO(obj), name)
  override def setField(obj: OV, name: FieldName, v: V): JOption[J, Unit] = ops.setField(extractO(obj), name, v)

  override def invokeMethod(callData: CallData)(callingClass: CF, staticClass: CF, mthName: MthName, sig: MthSig, obj: OV, args: Seq[V])(invoke: (OV, Mth, Seq[V]) => V): V =
    def liftedInvoke = (uov: UOV, mth: Mth, vs: Seq[V]) => invoke(injectO(uov), mth, vs)
    ops.invokeMethod(callData)(callingClass, staticClass, mthName, sig, extractO(obj), args)(liftedInvoke)

  override def makeNull(): OV = injectO(ops.makeNull())

  override def isNull(obj: OV): B = injectB(ops.isNull(extractO(obj)))


