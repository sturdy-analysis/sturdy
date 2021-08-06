package sturdy.values.ints

import sturdy.effect.failure.{Failure, FailureKind}

import scala.util.Random
import scala.math.{BigInt, log}

case object IntDivisionByZero extends FailureKind

trait IntOps[V](using Failure):
  def intLit(i: Int): V
  def randomInt(): V
  def abs(i: V): V
  def floor(i: V): V
  def ceiling(i: V): V
  def quotient(v1: V, v2: V): V
  def remainder(v1: V, v2: V): V
  def modulo(v1: V, v2: V): V
  def max(v1: V, v2: V): V
  def min(v1: V, v2: V): V
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V
  def gcd(v1: V, v2: V): V
  def lcm(v1: V, v2: V): V

trait IntDoubleOps[V, D]:
  def log(i: V): D

trait IntBoolOps[V, B]:
  def isZero(v1: V): B
  def isPositive(v1: V): B
  def isNegative(v1: V): B
  def isOdd(v1: V): B
  def isEven(v1: V): B

given ConcreteIntOps(using f: Failure): IntOps[Int] with
  def intLit(i: Int): Int = i
  def randomInt(): Int = Random.nextInt()
  def abs(v1: Int): Int = v1.abs
  def floor(v1: Int): Int = v1
  def ceiling(v1: Int): Int = v1
  def quotient(v1: Int, v2: Int): Int = div(v1,v2)
  def remainder(v1: Int, v2: Int): Int = abs(v1 % v2) // might be wrong
  def modulo(v1: Int, v2: Int): Int = v1 % v2
  def max(v1: Int, v2: Int): Int = v1.max(v2)
  def min(v1: Int, v2: Int): Int = v1.min(v2)
  def add(v1: Int, v2: Int): Int = v1 + v2
  def sub(v1: Int, v2: Int): Int = v1 - v2
  def mul(v1: Int, v2: Int): Int = v1 * v2
  def div(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
    else
      v1 / v2
  def gcd(v1: Int, v2: Int): Int = BigInt(v1).gcd(BigInt(v2)).toInt
  def lcm(v1: Int, v2: Int): Int = (v1 * v2).abs / gcd(v1,v2)

given ConcreteIntDoubleOps: IntDoubleOps[Int, Double] with
  override def log(i: Int): Double = math.log(i.toDouble)

given ConcreteIntBoolOps: IntBoolOps[Int, Boolean] with
  override def isZero(v1: Int): Boolean = v1 == 0
  override def isPositive(v1: Int): Boolean = v1 >= 0
  override def isNegative(v1: Int): Boolean = v1 < 0
  override def isOdd(v1: Int): Boolean = v1 % 2 == 0
  override def isEven(v1: Int): Boolean = v1 % 2 == 1