package sturdy.values.arrays

import sturdy.data.MayJoin.NoJoin
import sturdy.data.noJoin
import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocator
import sturdy.values.Structural
import sturdy.values.objects.TypeOps
import sturdy.values.ordering.EqOps

trait ArrayOps[ArrayIdentifier, Index, Value, ArrayValue, ArrayType, Site, ArrayOpContext, J[_] <: MayJoin[_]]:
  def makeArray(aid: ArrayIdentifier, vals: Seq[(Value, Site)], arrayType: ArrayType, arraySize: Value): ArrayValue
  def getVal(ctx: ArrayOpContext)(array: ArrayValue, idx: Index): JOption[J, Value]
  def setVal(ctx: ArrayOpContext)(array: ArrayValue, idx: Index, v: Value): JOption[J, Unit]
  def arrayLength(ctx: ArrayOpContext)(array: ArrayValue): Value
  def initArray(size: Index): Seq[Any]
  def arraycopy(src: ArrayValue, srcPos: Index, dest: ArrayValue, destPos: Index, length: Index): JOption[J, Unit]
  def getArray(ctx: ArrayOpContext)(array: ArrayValue): Seq[JOption[J, Value]]
  def printString(letters: Seq[Index]): Unit

case class Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize](aid: ArrayIdentifier, vals: Vector[ArrayElemAddr], arrayType: ArrayType, arraySize: ArraySize)

given structuralArray[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]: Structural[Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]] with {}

/*given ConcreteArrayOps[Addr, AID, V, AType, Site]
  (using alloc: Allocator[Addr, Site], store: Store[Addr, V, NoJoin]): ArrayOps[AID, Int, V, ConcreteRefValue, AType, Site, NoJoin] with
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

given ArrayEqOps[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]: EqOps[Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize], Boolean] with
  override def equ(v1: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize], v2: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]): Boolean = v1.aid == v2.aid
  override def neq(v1: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize], v2: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]): Boolean = v1.aid != v2.aid
