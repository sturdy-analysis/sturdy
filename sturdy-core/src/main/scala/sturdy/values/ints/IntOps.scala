package sturdy.values.ints

import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureKind

import scala.util.Random

case object IntDivisionByZero extends FailureKind

trait IntOps[V]:
  def intLit(i: Int): V
  def randomInt(): V
  
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  
  def div(v1: V, v2: V): V
  def divUnsigned(v1: V, v2: V): V
  def remainder(v1: V, v2: V): V
  def remainderUnsigned(v1: V, v2: V): V
  
  def bitAnd(v1: V, v2: V): V
  def bitOr(v1: V, v2: V): V
  def bitXor(v1: V, v2: V): V
  def shiftLeft(v: V, shift: V): V
  def shiftRight(v: V, shift: V): V
  def shiftRightUnsigned(v: V, shift: V): V
  def rotateLeft(v: V, shift: V): V
  def rotateRight(v: V, shift: V): V
  def countLeadingZeros(v: V): V
  def countTrailinZeros(v: V): V
  def nonzeroBitCount(v: V): V

given concreteIntOps(using f: Failure): IntOps[Int] with
  def intLit(i: Int): Int = i
  def randomInt(): Int = Random.nextInt()

  def add(v1: Int, v2: Int): Int = v1 + v2
  def sub(v1: Int, v2: Int): Int = v1 - v2
  def mul(v1: Int, v2: Int): Int = v1 * v2

  def div(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
    else
      v1 / v2
  def divUnsigned(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
    else
      Integer.divideUnsigned(v1, v2)
  def remainder(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
    else
      v1 % v2
  def remainderUnsigned(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
    else
      Integer.remainderUnsigned(v1, v2)

  def bitAnd(v1: Int, v2: Int): Int = v1 & v2
  def bitOr(v1: Int, v2: Int): Int = v1 | v2
  def bitXor(v1: Int, v2: Int): Int = v1 ^ v2
  def shiftLeft(v: Int, shift: Int): Int = v << shift
  def shiftRight(v: Int, shift: Int): Int = v >> shift
  def shiftRightUnsigned(v: Int, shift: Int): Int = v >>> shift
  def rotateLeft(v: Int, shift: Int): Int = Integer.rotateLeft(v, shift)
  def rotateRight(v: Int, shift: Int): Int = Integer.rotateRight(v, shift)
  def countLeadingZeros(v: Int): Int = Integer.numberOfLeadingZeros(v)
  def countTrailinZeros(v: Int): Int = Integer.numberOfTrailingZeros(v)
  def nonzeroBitCount(v: Int): Int = Integer.bitCount(v)
