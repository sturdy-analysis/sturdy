package sturdy.values.rationals

import sturdy.effect.failure.FailureKind
import sturdy.values.convert.{Convert, NilCC}

case object RationalDivisionByZero extends FailureKind

trait RationalOps[V]:
  def rationalLit(i1: Int, i2: Int): V
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V

  def max(v1: V, v2: V): V
  def min(v1: V, v2: V): V

  def absolute(v: V): V
  def floor(v: V): V
  def ceil(v: V): V


type ConvertIntRational[VFrom, VTo] = Convert[Int, Rational, VFrom, VTo, NilCC.type]
type ConvertRationalInt[VFrom, VTo] = Convert[Rational, Int, VFrom, VTo, NilCC.type]
type ConvertDoubleRational[VFrom, VTo] = Convert[Double, Rational, VFrom, VTo, NilCC.type]
type ConvertRationalDouble[VFrom, VTo] = Convert[Rational, Double, VFrom, VTo, NilCC.type]
