package sturdy.values.objects

import sturdy.data.{JOption, JOptionC, MayJoin, NoJoin}
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.values.Structural
import sturdy.values.relational.EqOps

import scala.collection.mutable


trait ObjectOps[Addr, FieldName, OID, V, CF, O, OV, Site, Mth, MthName, MthSig, NV, J[_] <: MayJoin[_]]:
  def makeObject(oid: OID, cfs: CF, vals: Seq[(V,Site,FieldName)]): OV
  def getField(obj: OV, name: FieldName): JOption[J, V]
  def setField(obj: OV, name: FieldName, v: V): JOption[J, Unit]
  def invokeFunction(obj: OV, mth: Mth, args: Seq[V])(invoke: (O, Mth, Seq[V]) => JOptionC[V]): JOptionC[V]
  def findFunction(obj: OV, name: MthName, sig: MthSig)(find: (O, MthName, MthSig) => Mth): Mth
  def makeNull(): NV


case class Object[OID, CF, Addr, FieldName](oid: OID, cls: CF, fields: Map[FieldName, Addr])

given structuralObject[OID, CF, Addr, FieldName]: Structural[Object[OID, CF, Addr, FieldName]] with {}

given ConcreteObjectOps[Addr, FieldName, OID, V, Site, CF, Mth, MthName, MthSig]
    (using alloc: Allocation[Addr, Site], store: Store[Addr, V, NoJoin]): ObjectOps[Addr, FieldName, OID, V, CF, Object[OID,CF,Addr,FieldName], Object[OID,CF,Addr,FieldName], Site, Mth, MthName, MthSig, Null, NoJoin] with
  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V,Site,FieldName)]): Object[OID, CF, Addr, FieldName] =
    val fieldAddrs = vals.map { (v, site, name) =>
      val addr = alloc(site)
      store.write(addr, v)
      (name,addr)
    }.toVector.toMap
    Object(oid, cfs, fieldAddrs)

  override def getField(obj: Object[OID, CF, Addr, FieldName], name: FieldName): JOption[NoJoin, V] =
    if (!obj.fields.contains(name))
      JOptionC.none
    else
      store.read(obj.fields(name))

  override def setField(obj: Object[OID, CF, Addr, FieldName], name: FieldName, v: V): JOptionC[Unit] =
    if (!obj.fields.contains(name))
      JOptionC.none
    else {
      store.write(obj.fields(name), v)
      JOptionC.some(())
    }
  override def invokeFunction(obj: Object[OID, CF, Addr, FieldName], mth: Mth, args: Seq[V])
                             (invoke: (Object[OID, CF, Addr, FieldName], Mth, Seq[V]) => JOptionC[V]): JOptionC[V] =
    invoke(obj, mth, args)

  override def findFunction(obj: Object[OID, CF, Addr, FieldName], name: MthName, sig: MthSig)
                           (find: (Object[OID, CF, Addr, FieldName], MthName, MthSig) => Mth): Mth =
    find(obj, name, sig)

  override def makeNull(): Null = null

  //override def isNull(nullVal: Null): Boolean = nullVal == null


given ObjectEqOps[OID, CF, Addr, FieldName]: EqOps[Object[OID, CF, Addr, FieldName], Boolean] with
  override def equ(v1: Object[OID, CF, Addr, FieldName], v2: Object[OID, CF, Addr, FieldName]): Boolean = v1.oid == v2.oid
  override def neq(v1: Object[OID, CF, Addr, FieldName], v2: Object[OID, CF, Addr, FieldName]): Boolean = v1.oid != v2.oid

