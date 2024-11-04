package sturdy.values.arrays

import sturdy.data.MayJoin.NoJoin
import sturdy.data.noJoin
import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.values.Structural
import sturdy.values.objects.TypeOps
import sturdy.values.relational.EqOps

trait ArrayOps[AID, Idx, V, AV, AType, Site, J[_] <: MayJoin[_]]:
  def makeArray(aid: AID, vals: Seq[(V, Site)], arrayType: AType, arraySize: V): AV
  def getVal(array: AV, idx: Idx): JOption[J, V]
  def setVal(array: AV, idx: Idx, v: V): JOption[J, Unit]
  def arrayLength(array: AV): V
  def initArray(size: Idx): Seq[Any]
  def arraycopy(src: AV, srcPos: Idx, dest: AV, destPos: Idx, length: Idx): JOption[J, Unit]
  def getArray(array: AV): Seq[JOption[J, V]]

case class Array[AID, ArrayElemAddr, AType, ASize](aid: AID, vals: Vector[ArrayElemAddr], arrayType: AType, arraySize: ASize)

given structuralArray[AID, Addr, AType, ASize]: Structural[Array[AID, Addr, AType, ASize]] with {}

/*given ConcreteArrayOps[Addr, AID, V, AType, Site]
  (using alloc: Allocation[Addr, Site], store: Store[Addr, V, NoJoin]): ArrayOps[AID, Int, V, ConcreteRefValue, AType, Site, NoJoin] with
  override def makeArray(aid: AID, vals: Seq[(V, Site)], arrayType: AType, arraySize: V): Array[AID, Addr, AType, V] =
    val valAddrs = vals.map{ (v, site) =>
      val addr = alloc(site)
      store.write(addr, v)
      addr
    }.toVector
    Array(aid, valAddrs, arrayType, arraySize)
  override def getVal(array: Array[AID, Addr, AType, V], idx: Int): JOption[NoJoin, V] =
    if (idx >= array.vals.size)
      JOptionC.none
    else
      store.read(array.vals(idx))
  override def setVal(array: Array[AID, Addr, AType, V], idx: Int, v: V): JOptionC[Unit] =
    if (idx >= array.vals.size)
      JOptionC.none
    else {
      store.write(array.vals(idx), v)
      JOptionC.some(())
    }

  override def arrayLength(array: Array[AID, Addr, AType, V]): V =
    array.arraySize

  override def initArray(size: Int): Seq[Any] =
    Seq.fill(size){}

  override def arraycopy(src: Array[AID, Addr, AType, V], srcPos: Int, dest: Array[AID, Addr, AType, V], destPos: Int, length: Int): JOption[MayJoin.NoJoin, Unit] =
    for (i <- 0 until length){
      if(srcPos+i >= src.vals.size || destPos+i >= dest.vals.size){
        return JOptionC.none
      }
      else{
        val toCopy = store.read(src.vals(srcPos + i)).get
        store.write(dest.vals(destPos + i), toCopy)
      }
    }
    JOptionC.some(())

  override def getArray(array: Array[AID, Addr, AType, V]): Seq[JOption[NoJoin, V]] =
    val arrayVals = array.vals.map(addr => getVal(array, array.vals.indexOf(addr)))
    arrayVals*/

given ArrayEqOps[AID, Addr, AType, V]: EqOps[Array[AID, Addr, AType, V], Boolean] with
  override def equ(v1: Array[AID, Addr, AType, V], v2: Array[AID, Addr, AType, V]): Boolean = v1.aid == v2.aid
  override def neq(v1: Array[AID, Addr, AType, V], v2: Array[AID, Addr, AType, V]): Boolean = v1.aid != v2.aid

class ConcreteArrayTypeOps[AID, Addr, AType, Type, V](f: (AType, Type) => Boolean) extends TypeOps[Array[AID, Addr, AType, V], Type, Boolean]:
  override def instanceOf(v: Array[AID, Addr, AType, V], target: Type): Boolean = f(v.arrayType, target)

