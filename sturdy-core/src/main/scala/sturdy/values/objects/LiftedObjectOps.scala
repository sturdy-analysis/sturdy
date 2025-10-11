package sturdy.values.objects

import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.effect.failure.Failure
import sturdy.effect.store.Store

class LiftedObjectOps[FieldName, OID, V, CF, OV, Site, Mth, MthName, MthSig, B, InvokeContext, FieldAccessContext, J[_] <: MayJoin[_], UOV, UB]
  (extractO: OV => UOV, injectO: UOV => OV, extractB: B => UB, injectB: UB => B)
  (using ops: ObjectOps[FieldName, OID, V, CF, UOV, Site, Mth, MthName, MthSig, UB, InvokeContext, FieldAccessContext, J]) extends ObjectOps[FieldName, OID, V, CF, OV, Site, Mth, MthName, MthSig, B, InvokeContext, FieldAccessContext, J]:

  override def makeObject(oid: OID, c: CF, vals: Seq[(V, Site, FieldName)]): OV = injectO(ops.makeObject(oid, c, vals))
  override def getField(context: FieldAccessContext)(obj: OV, identifier: FieldName): V = ops.getField(context: FieldAccessContext)(extractO(obj), identifier)
  override def setField(context: FieldAccessContext)(obj: OV, identifier: FieldName, v: V): JOption[J, Unit] = ops.setField(context: FieldAccessContext)(extractO(obj), identifier, v)

  override def invokeMethod(context: InvokeContext)(staticClass: CF, mthName: MthName, sig: MthSig, obj: OV, args: Seq[V])(invoke: (OV, Mth, Seq[V]) => V): V =
    def liftedInvoke = (uov: UOV, mth: Mth, vs: Seq[V]) => invoke(injectO(uov), mth, vs)
    ops.invokeMethod(context)(staticClass, mthName, sig, extractO(obj), args)(liftedInvoke)

  override def makeNull(): OV = injectO(ops.makeNull())

  override def isNull(obj: OV): B = injectB(ops.isNull(extractO(obj)))


