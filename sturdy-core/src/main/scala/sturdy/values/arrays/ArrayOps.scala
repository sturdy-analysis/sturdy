package sturdy.values.arrays

import sturdy.data.MayJoin.NoJoin
import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.values.relational.EqOps

trait ArrayOps[Addr, AID, Idx, V, A, AV, AType, Site, J[_] <: MayJoin[_]]:
  def makeArray(aid: AID, vals: Seq[(V, Site)], arrayType: AType): AV
  def getVal(array: AV, idx: Idx): JOption[J, V]
  def setVal(array: AV, idx: Idx, v: V): JOption[J, Unit]
  def arrayLength(array: AV): Int
  def initArray(size: Idx): Seq[Any]
  def checkType(array: AV, check: AType)(checkFun: (A, AType) => Boolean): Boolean

case class Array[AID, Addr, AType](aid: AID, vals: Vector[Addr], arrayType: AType)

given ConcreteArrayOps[Addr, AID, V, AType, Site]
  (using alloc: Allocation[Addr, Site], store: Store[Addr, V, NoJoin]): ArrayOps[Addr, AID, Int, V, Array[AID, Addr, AType], Array[AID, Addr, AType], AType, Site, NoJoin] with
  override def makeArray(aid: AID, vals: Seq[(V, Site)], arrayType: AType): Array[AID, Addr, AType] =
    val valAddrs = vals.map{ (v, site) =>
      val addr = alloc(site)
      store.write(addr, v)
      addr
    }.toVector
    Array(aid, valAddrs, arrayType)
  override def getVal(array: Array[AID, Addr, AType], idx: Int): JOption[NoJoin, V] =
    if (idx >= array.vals.size)
      JOptionC.none
    else
      store.read(array.vals(idx))
  override def setVal(array: Array[AID, Addr, AType], idx: Int, v: V): JOptionC[Unit] =
    if (idx >= array.vals.size)
      JOptionC.none
    else {
      store.write(array.vals(idx), v)
      JOptionC.some(())
    }
  override def arrayLength(array: Array[AID, Addr, AType]): Int =
    array.vals.size
  override def initArray(size: Int): Seq[Any] =
    Seq.fill(size){}

  override def checkType(array: Array[AID, Addr, AType], check: AType)(checkFun: (Array[AID, Addr, AType], AType) => Boolean): Boolean =
    checkFun(array, check)

given ArrayEqOps[AID, Addr, AType]: EqOps[Array[AID, Addr, AType], Boolean] with
  override def equ(v1: Array[AID, Addr, AType], v2: Array[AID, Addr, AType]): Boolean = v1.aid == v2.aid
  override def neq(v1: Array[AID, Addr, AType], v2: Array[AID, Addr, AType]): Boolean = v1.aid != v2.aid
