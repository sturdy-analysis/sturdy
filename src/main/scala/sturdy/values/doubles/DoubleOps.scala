package sturdy.values.doubles

import sturdy.effect.failure.Failure
import scala.util.Random

trait DoubleOps[V](using Failure):
  def numLit(d: Double): V
  def randomDouble(): V
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V

given ConcreteDoubleOps(using f: Failure): DoubleOps[Double] with
  def numLit(d: Double): Double = d
  def randomDouble(): Double = Random.nextDouble()
  def add(v1: Double, v2: Double): Double = v1 + v2
  def sub(v1: Double, v2: Double): Double = v1 - v2
  def mul(v1: Double, v2: Double): Double = v1 * v2
  def div(v1: Double, v2: Double): Double =
    if (v2 == 0)
      f.fail(s"Division by zero: $v1 / $v2")
    else
      v1 / v2
