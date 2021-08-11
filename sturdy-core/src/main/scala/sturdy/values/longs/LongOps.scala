package sturdy.values.longs

import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureKind

import scala.util.Random

case object LongDivisionByZero extends FailureKind

trait LongOps[V]:
  def longLit(l: Long): V
  def randomLong(): V

  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V

  def div(v1: V, v2: V): V
  def divUnsigned(v1: V, v2: V): V = ???
  def remainder(v1: V, v2: V): V = ???
  def remainderUnsigned(v1: V, v2: V): V = ???

  def bitAnd(v1: V, v2: V): V = ???
  def bitOr(v1: V, v2: V): V = ???
  def bitXor(v1: V, v2: V): V = ???
  def shiftLeft(v: V, shift: V): V = ???
  def shiftRight(v: V, shift: V): V = ???
  def shiftRightUnsigned(v: V, shift: V): V = ???
  def rotateLeft(v: V, shift: V): V = ???
  def rotateRight(v: V, shift: V): V = ???
  def countLeadingZeros(v: V): V = ???
  def countTrailinZeros(v: V): V = ???
  def nonzeroBitCount(v: V): V = ???


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
