package sturdy.values.longs

import sturdy.effect.failure.FailureKind
import sturdy.values.config
import sturdy.values.convert.Convert

case object LongDivisionByZero extends FailureKind
case object LongOverflow extends FailureKind

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

type ConvertLongInt[VFrom, VTo] = Convert[Long, Int, VFrom, VTo, Unit]
type ConvertLongFloat[VFrom, VTo] = Convert[Long, Float, VFrom, VTo, config.Bits]
type ConvertLongDouble[VFrom, VTo] = Convert[Long, Double, VFrom, VTo, config.Bits]
