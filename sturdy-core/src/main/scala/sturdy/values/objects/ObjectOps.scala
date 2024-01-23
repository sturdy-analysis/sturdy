package sturdy.values.objects

import sturdy.data.{JOption, JOptionC, MayJoin, NoJoin}
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation

import scala.collection.mutable


trait ObjectOps[Addr, Idx, V, CF, O, Site, J[_] <: MayJoin[_]]:
  def makeObject(cfs: CF, vals: Seq[(V,Site)]): O
  def getField(obj: O, idx: Idx): JOption[J, V]
  def setField(obj: O, idx: Idx, v: V): JOption[J, Unit]

case class Object[CF, Addr](cls: CF, fields: Vector[Addr])

given ConcreteObjectOps[Addr, V, Site, CF]
    (using alloc: Allocation[Addr, Site], store: Store[Addr, V, NoJoin]): ObjectOps[Addr, Int, V, CF, Object[CF,Addr], Site, NoJoin] with
  override def makeObject(cfs: CF, vals: Seq[(V,Site)]): Object[CF, Addr] =
    val fieldAddrs = vals.map { (v, site) =>
      val addr = alloc(site)
      store.write(addr, v)
      addr
    }.toVector
    Object(cfs, fieldAddrs)

  override def getField(obj: Object[CF, Addr], idx: Int): JOption[NoJoin, V] =
    if (idx >= obj.fields.size)
      JOptionC.none
    else
      store.read(obj.fields(idx))

  override def setField(obj: Object[CF, Addr], idx: Int, v: V): JOptionC[Unit] =
    if (idx >= obj.fields.size)
      JOptionC.none
    else {
      store.write(obj.fields(idx), v)
      JOptionC.some(())
    }

