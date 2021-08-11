package sturdy.values.longs

import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureKind

import scala.util.Random

import java.lang.{Long => JLong}

case object LongDivisionByZero extends FailureKind

trait LongOps[V]:
  def longLit(l: Long): V
  def randomLong(): V

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


given ConcreteLongOps(using f: Failure): LongOps[Long] with
  def longLit(l: Long): Long = l
  def randomLong(): Long = Random.nextLong()
  def add(v1: Long, v2: Long): Long = v1 + v2
  def sub(v1: Long, v2: Long): Long = v1 - v2
  def mul(v1: Long, v2: Long): Long = v1 * v2
  def div(v1: Long, v2: Long): Long =
    if (v2 == 0)
      f.fail(LongDivisionByZero, s"$v1 / $v2")
    else
      v1 / v2
  def divUnsigned(v1: Long, v2: Long): Long =
    if (v2 == 0)
      f.fail(LongDivisionByZero, s"$v1 / $v2")
    else
      JLong.divideUnsigned(v1, v2)
  def remainder(v1: Long, v2: Long): Long =
    if (v2 == 0)
      f.fail(LongDivisionByZero, s"$v1 / $v2")
    else
      v1 % v2
  def remainderUnsigned(v1: Long, v2: Long): Long =
    if (v2 == 0)
      f.fail(LongDivisionByZero, s"$v1 / $v2")
    else
      JLong.remainderUnsigned(v1, v2)

  def bitAnd(v1: Long, v2: Long): Long = v1 & v2
  def bitOr(v1: Long, v2: Long): Long = v1 | v2
  def bitXor(v1: Long, v2: Long): Long = v1 ^ v2
  def shiftLeft(v: Long, shift: Long): Long = v << shift
  def shiftRight(v: Long, shift: Long): Long = v >> shift
  def shiftRightUnsigned(v: Long, shift: Long): Long = v >>> shift
  def rotateLeft(v: Long, shift: Long): Long = JLong.rotateLeft(v, shift.toInt)
  def rotateRight(v: Long, shift: Long): Long = JLong.rotateRight(v, shift.toInt)
  def countLeadingZeros(v: Long): Long = JLong.numberOfLeadingZeros(v)
  def countTrailinZeros(v: Long): Long = JLong.numberOfTrailingZeros(v)
  def nonzeroBitCount(v: Long): Long = JLong.bitCount(v)
