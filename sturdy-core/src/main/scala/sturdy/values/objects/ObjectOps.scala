package sturdy.values.objects

import sturdy.data.{JOption, JOptionC, MayJoin, NoJoin}
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.values.relational.EqOps

import scala.collection.mutable


trait ObjectOps[Addr, Idx, OID, V, CF, O, OV, Site, Mth, J[_] <: MayJoin[_]]:
  def makeObject(oid: OID, cfs: CF, vals: Seq[(V,Site)]): OV
  def getField(obj: OV, idx: Idx): JOption[J, V]
  def setField(obj: OV, idx: Idx, v: V): JOption[J, Unit]
  def invokeFunction(obj: OV, mth: Mth, args: Seq[V])(invoke: (O, Mth, Seq[V]) => JOptionC[V]): JOptionC[V]

case class Object[OID, CF, Addr](oid: OID, cls: CF, fields: Vector[Addr])

given ConcreteObjectOps[Addr, OID, V, Site, CF, Mth]
    (using alloc: Allocation[Addr, Site], store: Store[Addr, V, NoJoin]): ObjectOps[Addr, Int, OID, V, CF, Object[OID,CF,Addr], Object[OID,CF,Addr], Site, Mth, NoJoin] with
  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V,Site)]): Object[OID, CF, Addr] =
    val fieldAddrs = vals.map { (v, site) =>
      val addr = alloc(site)
      store.write(addr, v)
      addr
    }.toVector
    Object(oid, cfs, fieldAddrs)

  override def getField(obj: Object[OID, CF, Addr], idx: Int): JOption[NoJoin, V] =
    if (idx >= obj.fields.size)
      JOptionC.none
    else
      store.read(obj.fields(idx))

  override def setField(obj: Object[OID, CF, Addr], idx: Int, v: V): JOptionC[Unit] =
    if (idx >= obj.fields.size)
      JOptionC.none
    else {
      store.write(obj.fields(idx), v)
      JOptionC.some(())
    }
  override def invokeFunction(obj: Object[OID, CF, Addr], mth: Mth, args: Seq[V])(invoke: (Object[OID, CF, Addr], Mth, Seq[V]) => JOptionC[V]): JOptionC[V] =
    invoke(obj, mth, args)

given ObjectEqOps[OID, CF, Addr]: EqOps[Object[OID, CF, Addr], Boolean] with
  override def equ(v1: Object[OID, CF, Addr], v2: Object[OID, CF, Addr]): Boolean = v1.oid == v2.oid
  override def neq(v1: Object[OID, CF, Addr], v2: Object[OID, CF, Addr]): Boolean = v1.oid != v2.oid
