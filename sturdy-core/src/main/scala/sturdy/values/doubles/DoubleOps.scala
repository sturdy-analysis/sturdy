package sturdy.values.doubles

import scala.util.Random

trait DoubleOps[V]:
  def doubleLit(d: Double): V
  def randomDouble(): V

  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V
  def min(v1: V, v2: V): V = ???
  def max(v1: V, v2: V): V = ???

  def absolute(v: V): V = ???
  def negated(v: V): V = ???
  def sqrt(v: V): V = ???
  def ceil(v: V): V = ???
  def floor(v: V): V = ???
  def truncate(v: V): V = ???
  def nearest(v: V): V = ???
  def copysign(v: V, sign: V): V = ???

given ConcreteDoubleOps: DoubleOps[Double] with
  def doubleLit(d: Double): Double = d
  def randomDouble(): Double = Random.nextDouble()
  def add(v1: Double, v2: Double): Double = v1 + v2
  def sub(v1: Double, v2: Double): Double = v1 - v2
  def mul(v1: Double, v2: Double): Double = v1 * v2
  def div(v1: Double, v2: Double): Double = v1 / v2
