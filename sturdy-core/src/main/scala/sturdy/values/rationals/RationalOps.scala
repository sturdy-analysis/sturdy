package sturdy.values.rationals

import sturdy.effect.failure.{Failure, FailureKind}

import scala.math.Numeric.BigDecimalIsFractional
import scala.util.Random
import scala.math.{log, BigInt}

case object RatioDivisionByZero extends FailureKind

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

given concreteRationalOps(using f: Failure): RationalOps[(Int,Int)] with
  override def rationalLit(i1: Int, i2: Int): (Int, Int) =
    if (i2 == 0) f.fail(RatioDivisionByZero, s"$i1 / $i2")
    else reduceFraction(i1, i2)
  override def add(i1: (Int, Int), i2: (Int, Int)): (Int,Int) =
    val numer = (i1._1 * i2._2 + i2._1 * i1._2)
    val denom = (i1._2 * i2._2)
    reduceFraction(numer, denom)
  override def sub(i1: (Int, Int), i2: (Int, Int)): (Int,Int) =
    val numer = (i1._1 * i2._2 - i2._1 * i1._2)
    val denom = (i1._2 * i2._2)
    reduceFraction(numer, denom)
  override def mul(i1: (Int, Int), i2: (Int, Int)): (Int,Int) =
    val numer = (i1._1 * i2._1)
    val denom = (i1._2 * i2._2)
    reduceFraction(numer, denom)
  override def div(i1: (Int, Int), i2: (Int, Int)): (Int,Int) =
    val numer = (i1._1 * i2._2)
    val denom = (i1._2 * i2._1)
    reduceFraction(numer, denom)
  override def max(i1: (Int, Int), i2: (Int, Int)): (Int,Int) =
    if i1._1/i1._2 >= i2._1/i2._2 then i1 else i2
  override def min(i1: (Int, Int), i2: (Int, Int)): (Int,Int) =
    if i1._1/i1._2 <= i2._1/i2._2 then i1 else i2

  override def absolute(i1: (Int, Int)): (Int, Int) =
    (i1._1.abs, i1._2.abs)
  override def floor(v: (Int, Int)): (Int, Int) =
    if (v._2 == 1)
      v
    else
      (Math.floorDiv(v._1, v._2), 1)
  override def ceil(v: (Int, Int)): (Int, Int) =
    val (x, y) = v
    if (y == 1)
      v
    else {
      var r = v._1 / v._2
      // if the signs are different and modulo not zero, round up
      if ((x ^ y) < 0 && (r * y != x)) r += 1
      (r, 1)
    }

trait ConvertIntRationalOps[I, R]:
  def intToRational(i: I): R
  def rationalToInt(r: R): I
given concreteConvertIntRationalOps: ConvertIntRationalOps[Int, (Int, Int)] with
  def intToRational(i: Int): (Int, Int) = (i, 1)
  def rationalToInt(r: (Int, Int)): Int = r._1 / r._2

trait ConvertDoubleRationalOps[D, R]:
  def doubleToRational(d: D): R
  def rationalToDouble(r: R): D
given concreteConvertDoubleRationalOps: ConvertDoubleRationalOps[Double, (Int, Int)] with
  def doubleToRational(d: Double): (Int, Int) = ???
  def rationalToDouble(r: (Int, Int)): Double = r._1.toDouble / r._2


def reduceFraction(i1: Int, i2: Int): (Int, Int) =
  val gcd = BigInt(i1).gcd(BigInt(i2))
  val r1 = (i1/gcd).toInt
  val r2 = (i2/gcd).toInt
  (r1,r2)
