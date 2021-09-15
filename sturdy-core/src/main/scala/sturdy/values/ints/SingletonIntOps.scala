package sturdy.values.ints

import sturdy.effect.failure.Failure
import sturdy.values.Singleton

given SingletonIntOps(using f: Failure): IntOps[Singleton[Int]] with
  val ops = ConcreteIntOps
  def intLit(i: Int): Singleton[Int] = Singleton.Single(i)
  def randomInt(): Singleton[Int] = Singleton.NoSingleton

  def add(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.add, v2)
  def sub(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.sub, v2)
  def mul(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.mul, v2)

  def max(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.max, v2)
  def min(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.min, v2)

  def div(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.div, v2)
  def divUnsigned(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.divUnsigned, v2)
  def remainder(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.remainder, v2)
  def remainderUnsigned(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.remainderUnsigned, v2)
  def modulo(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.modulo, v2)
  def gcd(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.gcd, v2)

  def absolute(v: Singleton[Int]): Singleton[Int] = v.unary(ops.absolute)
  def bitAnd(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.bitAnd, v2)
  def bitOr(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.bitOr, v2)
  def bitXor(v1: Singleton[Int], v2: Singleton[Int]): Singleton[Int] = v1.binary(ops.bitXor, v2)
  def shiftLeft(v: Singleton[Int], shift: Singleton[Int]): Singleton[Int] = v.binary(ops.shiftLeft, shift)
  def shiftRight(v: Singleton[Int], shift: Singleton[Int]): Singleton[Int] = v.binary(ops.shiftRight, shift)
  def shiftRightUnsigned(v: Singleton[Int], shift: Singleton[Int]): Singleton[Int] = v.binary(ops.shiftRightUnsigned, shift)
  def rotateLeft(v: Singleton[Int], shift: Singleton[Int]): Singleton[Int] = v.binary(ops.rotateLeft, shift)
  def rotateRight(v: Singleton[Int], shift: Singleton[Int]): Singleton[Int] = v.binary(ops.rotateRight, shift)
  def countLeadingZeros(v: Singleton[Int]): Singleton[Int] = v.unary(ops.countLeadingZeros)
  def countTrailinZeros(v: Singleton[Int]): Singleton[Int] = v.unary(ops.countTrailinZeros)
  def nonzeroBitCount(v: Singleton[Int]): Singleton[Int] = v.unary(ops.nonzeroBitCount)
  