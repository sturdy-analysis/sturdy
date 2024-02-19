package sturdy.values.arrays

import sturdy.data.MayJoin.NoJoin
import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.values.relational.EqOps

trait ArrayOps[Addr, AID, Idx, V, AV, Site, J[_] <: MayJoin[_]]:
  def makeArray(aid: AID, vals: Seq[(V, Site)]): AV
  def getVal(array: AV, idx: Idx): JOption[J, V]
  def setVal(array: AV, idx: Idx, v: V): JOption[J, Unit]
  def arrayLength(array: AV): Int
  def initArray(size: Idx): Seq[Any]

case class Array[AID, Addr](aid: AID, vals: Vector[Addr])

given ConcreteArrayOps[Addr, AID, V, Site]
  (using alloc: Allocation[Addr, Site], store: Store[Addr, V, NoJoin]): ArrayOps[Addr, AID, Int, V, Array[AID, Addr], Site, NoJoin] with
  override def makeArray(aid: AID, vals: Seq[(V, Site)]): Array[AID, Addr] =
    val valAddrs = vals.map{ (v, site) =>
      val addr = alloc(site)
      store.write(addr, v)
      addr
    }.toVector
    Array(aid, valAddrs)
  override def getVal(array: Array[AID, Addr], idx: Int): JOption[NoJoin, V] =
    if (idx >= array.vals.size)
      JOptionC.none
    else
      store.read(array.vals(idx))
  override def setVal(array: Array[AID, Addr], idx: Int, v: V): JOptionC[Unit] =
    if (idx >= array.vals.size)
      JOptionC.none
    else {
      store.write(array.vals(idx), v)
      JOptionC.some(())
    }
  override def arrayLength(array: Array[AID, Addr]): Int =
    array.vals.size
  override def initArray(size: Int): Seq[Any] =
    Seq.fill(size){}

given ArrayEqOps[AID, Addr]: EqOps[Array[AID, Addr], Boolean] with
  override def equ(v1: Array[AID, Addr], v2: Array[AID, Addr]): Boolean = v1.aid == v2.aid
  override def neq(v1: Array[AID, Addr], v2: Array[AID, Addr]): Boolean = v1.aid != v2.aid
