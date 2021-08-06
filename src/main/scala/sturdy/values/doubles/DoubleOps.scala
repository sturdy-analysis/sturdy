package sturdy.values.doubles

import scala.util.Random
import scala.math.{log}

trait DoubleOps[V]:
  def numLit(d: Double): V
  def randomDouble(): V
  def abs(v1: V): V
  def log(v1: V): V
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V
  def max(v1: V, v2: V): V
  def min(v1: V, v2: V): V

trait DoubleIntOps[V, I]:
  def floor(v1: V): I
  def ceiling(v1: V): I

trait DoubleBoolOps[V, B]:
  def isZero(v1: V): B
  def isPositive(v1: V): B
  def isNegative(v1: V): B

given ConcreteDoubleOps: DoubleOps[Double] with
  override def numLit(d: Double): Double = d
  override def randomDouble(): Double = Random.nextDouble()
  override def abs(v1: Double): Double = if v1 > 0 then v1 else -1 * v1
  override def log(v1: Double): Double = math.log(v1)
  override def add(v1: Double, v2: Double): Double = v1 + v2
  override def sub(v1: Double, v2: Double): Double = v1 - v2
  override def mul(v1: Double, v2: Double): Double = v1 * v2
  override def div(v1: Double, v2: Double): Double = v1 / v2
  override def max(v1: Double, v2: Double): Double = if v1 > v2 then v1 else v2
  override def min(v1: Double, v2: Double): Double = if v1 < v2 then v1 else v2

given ConcreteDoubleIntOps: DoubleIntOps[Double, Int] with
  def floor(v1: Double): Int = v1.floor.toInt
  def ceiling(v1: Double): Int = v1.ceil.toInt

given ConcreteDoubleBoolOps: DoubleBoolOps[Double, Boolean] with
  override def isZero(v1: Double): Boolean = v1 == 0
  override def isPositive(v1: Double): Boolean = v1 >= 0
  override def isNegative(v1: Double): Boolean = v1 < 0

