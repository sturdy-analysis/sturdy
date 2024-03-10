package sturdy.values.objects

import sturdy.data.{JOption, JOptionC, MayJoin, NoJoin}
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.values.relational.EqOps

import scala.collection.mutable


trait ObjectOps[Addr, Idx, FieldName, OID, V, CF, O, OV, Site, Mth, MthName, MthSig, NV, TypeRep, J[_] <: MayJoin[_]]:
  def makeObject(oid: OID, cfs: CF, vals: Seq[(V,Site,FieldName)]): OV
  def getField(obj: OV, name: FieldName): JOption[J, V]
  def setField(obj: OV, name: FieldName, v: V): JOption[J, Unit]
  def invokeFunction(obj: OV, mth: Mth, args: Seq[V])(invoke: (O, Mth, Seq[V]) => JOptionC[V]): JOptionC[V]
  def findFunction(obj: OV, name: MthName, sig: MthSig)(invoke: (O, MthName, MthSig) => Mth): Mth
  def makeNull(): NV
  def isNull(nullVal: NV): Boolean
  def checkType(obj: OV, check: TypeRep)(checkFun: (O, TypeRep) => Boolean): Boolean

case class Object[OID, CF, Addr, FieldName](oid: OID, cls: CF, fields: Map[FieldName, Addr])

given ConcreteObjectOps[Addr, OID, V, Site, CF, Mth, MthName, MthSig, TypeRep]
    (using alloc: Allocation[Addr, Site], store: Store[Addr, V, NoJoin]): ObjectOps[Addr, Int, String, OID, V, CF, Object[OID,CF,Addr,String], Object[OID,CF,Addr,String], Site, Mth, MthName, MthSig, Null, TypeRep, NoJoin] with
  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V,Site,String)]): Object[OID, CF, Addr, String] =
    val fieldAddrs = vals.map { (v, site, name) =>
      val addr = alloc(site)
      store.write(addr, v)
      (name,addr)
    }.toVector.toMap
    Object(oid, cfs, fieldAddrs)

  override def getField(obj: Object[OID, CF, Addr, String], name: String): JOption[NoJoin, V] =
    if (!obj.fields.contains(name))
      JOptionC.none
    else
      store.read(obj.fields(name))

  override def setField(obj: Object[OID, CF, Addr, String], name: String, v: V): JOptionC[Unit] =
    if (!obj.fields.contains(name))
      JOptionC.none
    else {
      store.write(obj.fields(name), v)
      JOptionC.some(())
    }
  override def invokeFunction(obj: Object[OID, CF, Addr, String], mth: Mth, args: Seq[V])(invoke: (Object[OID, CF, Addr, String], Mth, Seq[V]) => JOptionC[V]): JOptionC[V] =
    invoke(obj, mth, args)

  override def findFunction(obj: Object[OID, CF, Addr, String], name: MthName, sig: MthSig)(invoke: (Object[OID, CF, Addr, String], MthName, MthSig) => Mth): Mth =
    invoke(obj, name, sig)

  override def makeNull(): Null = null

  override def isNull(nullVal: Null): Boolean = nullVal == null

  override def checkType(obj: Object[OID, CF, Addr, String], check: TypeRep)(checkFun: (Object[OID, CF, Addr, String], TypeRep) => Boolean): Boolean =
    checkFun(obj, check)

given ObjectEqOps[OID, CF, Addr]: EqOps[Object[OID, CF, Addr, String], Boolean] with
  override def equ(v1: Object[OID, CF, Addr, String], v2: Object[OID, CF, Addr, String]): Boolean = v1.oid == v2.oid
  override def neq(v1: Object[OID, CF, Addr, String], v2: Object[OID, CF, Addr, String]): Boolean = v1.oid != v2.oid
