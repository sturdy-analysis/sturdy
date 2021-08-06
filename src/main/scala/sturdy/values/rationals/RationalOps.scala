package sturdy.values.rationals

import sturdy.effect.failure.{Failure, FailureKind}
import scala.util.Random
import scala.math.{BigInt, log}

case object RatioDivisionByZero extends FailureKind

trait RationalOps[V](using Failure):
  def rationalLit(i1: Int, i2: Int): V
  def abs(v1: V): V
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V
  def max(v1: V, v2: V): V
  def min(v1: V, v2: V): V

trait RationalIntOps[V, I]:
  def floor(v1: V): I
  def ceiling(v1: V): I

trait RationalDoubleOps[V, D]:
  def log(v1: V): D

trait RationalBoolOps[V, B]:
  def isZero(v: V): B
  def isPositive(v: V): B
  def isNegative(v: V): B

given ConcreteRationalOps(using f: Failure): RationalOps[(Int,Int)] with
  override def rationalLit(i1: Int, i2: Int): (Int, Int) =
    if (i2 == 0) f.fail(RatioDivisionByZero, s"$i1 / $i2")
    else (i1, i2)
  override def abs(i1: (Int, Int)): (Int, Int) =
    val r1 = if i1._1 >= 0 then i1._1 else -1 * i1._1
    val r2 = if i1._2 >= 0 then i1._2 else -1 * i1._2
    (r1, r2)
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

given ConcreteRationalIntOps: RationalIntOps[(Int, Int), Int] with
  override def floor(v1: (Int, Int)): Int = (v1._1 / v1._2).toDouble.floor.toInt
  override def ceiling(v1: (Int, Int)): Int = (v1._1 / v1._2).toDouble.ceil.toInt

given ConcreteRationalDoubleOps: RationalDoubleOps[(Int,Int), Double] with
  def log(i1: (Int, Int)): Double = math.log(i1._1 / i1._2)

given ConcreteIntBoolOps: RationalBoolOps[(Int,Int), Boolean] with
  override def isZero(v: (Int,Int)): Boolean = v._1 == 0
  override def isPositive(v: (Int,Int)): Boolean = v._1 / v._2 >= 0
  override def isNegative(v: (Int,Int)): Boolean = v._1 / v._2 < 0

def reduceFraction(i1: Int, i2: Int): (Int, Int) =
  val gcd = BigInt(i1).gcd(BigInt(i2))
  val r1 = (i1/gcd).toInt
  val r2 = (i2/gcd).toInt
  (r1,r2)