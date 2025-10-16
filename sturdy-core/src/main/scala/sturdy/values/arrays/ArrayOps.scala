package sturdy.values.arrays

import sturdy.data.{JOption, MayJoin}
import sturdy.values.Structural
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

given ArrayEqOps[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]: EqOps[Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize], Boolean] with
  override def equ(v1: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize], v2: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]): Boolean = v1.aid == v2.aid
  override def neq(v1: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize], v2: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]): Boolean = v1.aid != v2.aid
