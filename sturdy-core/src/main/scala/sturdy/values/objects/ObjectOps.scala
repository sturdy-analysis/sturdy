package sturdy.values.objects

import org.apache.commons.math3.geometry.partitioning.BoundaryProjection
import sturdy.data.MayJoin.WithJoin
import sturdy.data.{JOption, JOptionA, JOptionC, MayJoin, NoJoin}
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.values.Structural
import sturdy.values.relational.EqOps

import scala.collection.mutable


trait ObjectOps[FieldName, OID, V, CF, OV, Site, Mth, MthName, MthSig, NV, J[_] <: MayJoin[_]]:
  def makeObject(oid: OID, cfs: CF, vals: Seq[(V,Site,FieldName)]): OV
  def getField(obj: OV, name: FieldName): JOption[J, V]
  def setField(obj: OV, name: FieldName, v: V): JOption[J, Unit]
  def invokeFunctionCorrect(obj: OV, mthName: MthName, sig: MthSig, args: Seq[V])(invoke: (OV, Mth, Seq[V]) => V): V
  def makeNull(): NV


case class Object[OID, CF, FieldAddr, FieldName](oid: OID, cls: CF, fields: Map[FieldName, FieldAddr])

given structuralObject[OID, CF, Addr, FieldName]: Structural[Object[OID, CF, Addr, FieldName]] with {}

/*given ConcreteObjectOps[FieldAddr, FieldName, OID, V, Site, CF, Mth, MthName, MthSig]
    (using alloc: Allocation[FieldAddr, Site], store: Store[FieldAddr, V, NoJoin]): ObjectOps[FieldName, OID, V, CF, Object[OID,CF,FieldAddr,FieldName], Object[OID,CF,FieldAddr,FieldName], Site, Mth, MthName, MthSig, Null, NoJoin] with
  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V,Site,FieldName)]): Object[OID, CF, FieldAddr, FieldName] =
    val fieldAddrs = vals.map { (v, site, name) =>
      val addr = alloc(site)
      store.write(addr, v)
      (name,addr)
    }.toVector.toMap
    Object(oid, cfs, fieldAddrs)

  override def getField(obj: Object[OID, CF, FieldAddr, FieldName], name: FieldName): JOption[NoJoin, V] =
    if (!obj.fields.contains(name))
      JOptionC.none
    else
      store.read(obj.fields(name))

  override def setField(obj: Object[OID, CF, FieldAddr, FieldName], name: FieldName, v: V): JOptionC[Unit] =
    if (!obj.fields.contains(name))
      JOptionC.none
    else {
      store.write(obj.fields(name), v)
      JOptionC.some(())
    }
  override def invokeFunction(obj: Object[OID, CF, FieldAddr, FieldName], mth: Mth, args: Seq[V])
                             (invoke: (Object[OID, CF, FieldAddr, FieldName], Mth, Seq[V]) => V): V =
    invoke(obj, mth, args)

  override def findFunction(obj: Object[OID, CF, FieldAddr, FieldName], name: MthName, sig: MthSig)
                           (find: (Object[OID, CF, FieldAddr, FieldName], MthName, MthSig) => Mth): Mth =
    find(obj, name, sig)

  override def makeNull(): Null = null


given ConcreteObjectOpsWithJoin[FieldAddr, FieldName, OID, V, Site, CF, Mth, MthName, MthSig]
  (using fieldAlloc: Allocation[FieldAddr, Site], store: Store[FieldAddr, V, WithJoin]): ObjectOps[FieldName, OID, V, CF, Object[OID, CF, FieldAddr, FieldName], Object[OID, CF, FieldAddr, FieldName], Site, Mth, MthName, MthSig, Null, WithJoin] with
    override def makeObject(oid: OID, cfs: CF, vals: Seq[(V, Site, FieldName)]): Object[OID, CF, FieldAddr, FieldName] =
      val fieldAddrs = vals.map { (v, site, name) =>
        val addr = fieldAlloc(site)
        store.write(addr, v)
        (name, addr)
      }.toVector.toMap
      Object(oid, cfs, fieldAddrs)

    override def getField(obj: Object[OID, CF, FieldAddr, FieldName], name: FieldName): JOption[WithJoin, V] =
      if (!obj.fields.contains(name))
        JOptionA.none
      else
        store.read(obj.fields(name))

    override def setField(obj: Object[OID, CF, FieldAddr, FieldName], name: FieldName, v: V): JOptionA[Unit] =
      if (!obj.fields.contains(name))
        JOptionA.none
      else {
        store.write(obj.fields(name), v)
        JOptionA.some(())
      }

    override def invokeFunction(obj: Object[OID, CF, FieldAddr, FieldName], mth: Mth, args: Seq[V])
                               (invoke: (Object[OID, CF, FieldAddr, FieldName], Mth, Seq[V]) => V): V =
      invoke(obj, mth, args)

    override def findFunction(obj: Object[OID, CF, FieldAddr, FieldName], name: MthName, sig: MthSig)
                             (find: (Object[OID, CF, FieldAddr, FieldName], MthName, MthSig) => Mth): Mth =
      find(obj, name, sig)

    override def makeNull(): Null = null
*/

given ObjectEqOps[OID, CF, Addr, FieldName]: EqOps[Object[OID, CF, Addr, FieldName], Boolean] with
  override def equ(v1: Object[OID, CF, Addr, FieldName], v2: Object[OID, CF, Addr, FieldName]): Boolean = v1.oid == v2.oid
  override def neq(v1: Object[OID, CF, Addr, FieldName], v2: Object[OID, CF, Addr, FieldName]): Boolean = v1.oid != v2.oid

