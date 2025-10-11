package sturdy.values.objects

import sturdy.data.{JOption, MayJoin}

class LiftedObjectOps[FieldIdentifier, ObjectIdentifier, Value, Class, ObjectValue, Site, Method, MthName, MthSig, Boolean, InvokeContext, FieldAccessContext, J[_] <: MayJoin[_], UOV, UB]
(extractO: ObjectValue => UOV, injectO: UOV => ObjectValue, extractB: Boolean => UB, injectB: UB => Boolean)
(using ops: ObjectOps[FieldIdentifier, ObjectIdentifier, Value, Class, UOV, Site, Method, MthName, MthSig, UB, InvokeContext, FieldAccessContext, J])
  extends ObjectOps[FieldIdentifier, ObjectIdentifier, Value, Class, ObjectValue, Site, Method, MthName, MthSig, Boolean, InvokeContext, FieldAccessContext, J]:

  override def makeObject(oid: ObjectIdentifier, c: Class, fields: Seq[(Value, Site, FieldIdentifier)]): ObjectValue = injectO(ops.makeObject(oid, c, fields))

  override def getField(context: FieldAccessContext)(obj: ObjectValue, identifier: FieldIdentifier): Value = ops.getField(context: FieldAccessContext)(extractO(obj), identifier)

  override def setField(context: FieldAccessContext)(obj: ObjectValue, identifier: FieldIdentifier, v: Value): JOption[J, Unit] = ops.setField(context: FieldAccessContext)(extractO(obj), identifier, v)

  override def invokeMethod(context: InvokeContext)(staticClass: Class, mthName: MthName, sig: MthSig, obj: ObjectValue, args: Seq[Value])(invoke: (ObjectValue, Method, Seq[Value]) => Value): Value =
    def liftedInvoke = (uov: UOV, mth: Method, vs: Seq[Value]) => invoke(injectO(uov), mth, vs)

    ops.invokeMethod(context)(staticClass, mthName, sig, extractO(obj), args)(liftedInvoke)

  override def makeNull(): ObjectValue = injectO(ops.makeNull())

  override def isNull(obj: ObjectValue): Boolean = injectB(ops.isNull(extractO(obj)))
