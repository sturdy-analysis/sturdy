package sturdy.language.wasm.abstractions

import sturdy.language.wasm.generic.FuncId
import sturdy.values.integer.IntegerOps
import sturdy.values.{Powerset, Topped}
import swam.FuncType
import swam.syntax.Func

case class FunctionPointer(funcId: FuncId, function: Func, functionType: FuncType)
type FunctionPointers = Topped[Powerset[FunctionPointer]]

final class FunctionPointerIntegerOps extends IntegerOps[Int,FunctionPointers]:
  override def integerLit(i: Int): FunctionPointers = Topped.Top

  override def randomInteger(): FunctionPointers = Topped.Top

  override def add(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def sub(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def mul(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def max(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def min(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def absolute(v: FunctionPointers): FunctionPointers = Topped.Top

  override def div(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def divUnsigned(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def remainder(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def remainderUnsigned(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def modulo(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def gcd(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def bitAnd(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def bitOr(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def bitXor(v1: FunctionPointers, v2: FunctionPointers): FunctionPointers = Topped.Top

  override def shiftLeft(v: FunctionPointers, shift: FunctionPointers): FunctionPointers = Topped.Top

  override def shiftRight(v: FunctionPointers, shift: FunctionPointers): FunctionPointers = Topped.Top

  override def shiftRightUnsigned(v: FunctionPointers, shift: FunctionPointers): FunctionPointers = Topped.Top

  override def rotateLeft(v: FunctionPointers, shift: FunctionPointers): FunctionPointers = Topped.Top

  override def rotateRight(v: FunctionPointers, shift: FunctionPointers): FunctionPointers = Topped.Top

  override def countLeadingZeros(v: FunctionPointers): FunctionPointers = Topped.Top

  override def countTrailingZeros(v: FunctionPointers): FunctionPointers = Topped.Top

  override def nonzeroBitCount(v: FunctionPointers): FunctionPointers = Topped.Top

  override def invertBits(v: FunctionPointers): FunctionPointers = Topped.Top
