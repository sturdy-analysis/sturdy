package sturdy.values.arrays

import sturdy.data.{JOption, MayJoin}
import sturdy.values.Structural
import sturdy.values.ordering.EqOps

trait ArrayOps[ArrayIdentifier, Index, Value, ArrayValue, ArrayType, ArrayElemAllocSite, ArrayOpContext, J[_] <: MayJoin[_]]:
  def makeArray(aid: ArrayIdentifier, valueSupplier: Int => (Value, ArrayElemAllocSite), arrayType: ArrayType, arraySize: Index): ArrayValue

  def get(ctx: ArrayOpContext)(array: ArrayValue, idx: Index): JOption[J, Value]

  def set(ctx: ArrayOpContext)(array: ArrayValue, idx: Index, v: Value): JOption[J, Unit]

  def length(ctx: ArrayOpContext)(array: ArrayValue): Value

case class Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize](aid: ArrayIdentifier, vals: Seq[ArrayElemAddr], arrayType: ArrayType, arraySize: ArraySize)

given structuralArray[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]: Structural[Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]] with {}

given ArrayEqOps[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]: EqOps[Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize], Boolean] with
  override def equ(v1: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize], v2: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]): Boolean = v1.aid == v2.aid

  override def neq(v1: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize], v2: Array[ArrayIdentifier, ArrayElemAddr, ArrayType, ArraySize]): Boolean = v1.aid != v2.aid
