package sturdy.values.doubles

import scala.util.Random

trait DoubleOps[V]:
  def numLit(d: Double): V
  def randomDouble(): V
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V

given ConcreteDoubleOps: DoubleOps[Double] with
  def numLit(d: Double): Double = d
  def randomDouble(): Double = Random.nextDouble()
  def add(v1: Double, v2: Double): Double = v1 + v2
  def sub(v1: Double, v2: Double): Double = v1 - v2
  def mul(v1: Double, v2: Double): Double = v1 * v2
  def div(v1: Double, v2: Double): Double = v1 / v2
