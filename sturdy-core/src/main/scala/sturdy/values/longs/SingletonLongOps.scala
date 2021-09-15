package sturdy.values.longs

import sturdy.effect.failure.Failure
import sturdy.values.Singleton

given SingletonLongOps(using f: Failure): LongOps[Singleton[Long]] with
  val ops = ConcreteLongOps

  def longLit(l: Long): Singleton[Long] = Singleton.Single(l)
  def randomLong(): Singleton[Long] = Singleton.NoSingleton

  def add(v1: Singleton[Long], v2: Singleton[Long]): Singleton[Long] = v1.binary(ops.add, v2)
  def sub(v1: Singleton[Long], v2: Singleton[Long]): Singleton[Long] = v1.binary(ops.sub, v2)
  def mul(v1: Singleton[Long], v2: Singleton[Long]): Singleton[Long] = v1.binary(ops.mul, v2)


  def div(v1: Singleton[Long], v2: Singleton[Long]): Singleton[Long] = v1.binary(ops.div, v2)
  def divUnsigned(v1: Singleton[Long], v2: Singleton[Long]): Singleton[Long] = v1.binary(ops.divUnsigned, v2)
  def remainder(v1: Singleton[Long], v2: Singleton[Long]): Singleton[Long] = v1.binary(ops.remainder, v2)
  def remainderUnsigned(v1: Singleton[Long], v2: Singleton[Long]): Singleton[Long] = v1.binary(ops.remainderUnsigned, v2)

  def bitAnd(v1: Singleton[Long], v2: Singleton[Long]): Singleton[Long] = v1.binary(ops.bitAnd, v2)
  def bitOr(v1: Singleton[Long], v2: Singleton[Long]): Singleton[Long] = v1.binary(ops.bitOr, v2)
  def bitXor(v1: Singleton[Long], v2: Singleton[Long]): Singleton[Long] = v1.binary(ops.bitXor, v2)
  def shiftLeft(v: Singleton[Long], shift: Singleton[Long]): Singleton[Long] = v.binary(ops.shiftLeft, shift)
  def shiftRight(v: Singleton[Long], shift: Singleton[Long]): Singleton[Long] = v.binary(ops.shiftRight, shift)
  def shiftRightUnsigned(v: Singleton[Long], shift: Singleton[Long]): Singleton[Long] = v.binary(ops.shiftRightUnsigned, shift)
  def rotateLeft(v: Singleton[Long], shift: Singleton[Long]): Singleton[Long] = v.binary(ops.rotateLeft, shift)
  def rotateRight(v: Singleton[Long], shift: Singleton[Long]): Singleton[Long] = v.binary(ops.rotateRight, shift)
  def countLeadingZeros(v: Singleton[Long]): Singleton[Long] = v.unary(ops.countLeadingZeros)
  def countTrailinZeros(v: Singleton[Long]): Singleton[Long] = v.unary(ops.countTrailinZeros)
  def nonzeroBitCount(v: Singleton[Long]): Singleton[Long] = v.unary(ops.nonzeroBitCount)
  