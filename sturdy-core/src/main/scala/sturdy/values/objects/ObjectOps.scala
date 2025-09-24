package sturdy.values.objects

import sturdy.data.{JOption, MayJoin}
import sturdy.effect.failure.Failure
import sturdy.values.Structural
import sturdy.values.ordering.EqOps

trait ObjectOps[FieldIdentifier, ObjectIdentifier, Value, Class, ObjectValue, Site, Method, MthName, MthSig, Boolean, InvokeContext, J[_] <: MayJoin[_]]:
  def makeObject(oid: ObjectIdentifier, c: Class, vals: Seq[(Value, Site, FieldIdentifier)]): ObjectValue

  def getField(callingClass: Class, obj: ObjectValue, identifier: FieldIdentifier)(using Failure): Value

  def setField(callingClass: Class, obj: ObjectValue, identifier: FieldIdentifier, v: Value): JOption[J, Unit]

  def invokeMethod(context: InvokeContext)(callingClass: Class, staticClass: Class, mthName: MthName, sig: MthSig, obj: ObjectValue, args: Seq[Value])(invoke: (ObjectValue, Method, Seq[Value]) => Value): Value

  def makeNull(): ObjectValue

  def isNull(obj: ObjectValue): Boolean


case class Object[OID, CF, FieldAddr, FieldName](oid: OID, cls: CF, fields: Map[FieldName, FieldAddr])

given structuralObject[OID, CF, Addr, FieldName]: Structural[Object[OID, CF, Addr, FieldName]] with {}


given ObjectEqOps[OID, CF, Addr, FieldName]: EqOps[Object[OID, CF, Addr, FieldName], Boolean] with
  override def equ(v1: Object[OID, CF, Addr, FieldName], v2: Object[OID, CF, Addr, FieldName]): Boolean = v1.oid == v2.oid

  override def neq(v1: Object[OID, CF, Addr, FieldName], v2: Object[OID, CF, Addr, FieldName]): Boolean = v1.oid != v2.oid
