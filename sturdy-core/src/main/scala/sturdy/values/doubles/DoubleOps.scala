package sturdy.values.doubles

import scala.util.Random

trait DoubleOps[V]:
  def doubleLit(d: Double): V
  def randomDouble(): V

  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V
  
  def min(v1: V, v2: V): V
  def max(v1: V, v2: V): V

  def absolute(v: V): V
  def negated(v: V): V
  def sqrt(v: V): V
  def ceil(v: V): V
  def floor(v: V): V
  def truncate(v: V): V
  def nearest(v: V): V
  def copysign(v: V, sign: V): V
  
  def logNatural(v: V): V

given ConcreteDoubleOps: DoubleOps[Double] with
  def doubleLit(d: Double): Double = d
  def randomDouble(): Double = Random.nextDouble()
  def add(v1: Double, v2: Double): Double = v1 + v2
  def sub(v1: Double, v2: Double): Double = v1 - v2
  def mul(v1: Double, v2: Double): Double = v1 * v2
  def div(v1: Double, v2: Double): Double = v1 / v2

  def min(v1: Double, v2: Double): Double = Math.min(v1, v2)
  def max(v1: Double, v2: Double): Double = Math.max(v1, v2)

  def absolute(v: Double): Double = Math.abs(v)
  def negated(v: Double): Double = -v
  def sqrt(v: Double): Double = Math.sqrt(v)
  def ceil(v: Double): Double = v.ceil
  def floor(v: Double): Double = v.floor
  def truncate(v: Double): Double = if (v < 0) v.ceil else v.floor
  def nearest(v: Double): Double =
    // copied from https://github.com/satabin/swam/blob/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime/F64.scala#L30
    if (v.isInfinite || v.isNaN || v.isWhole)
      v
    else
      Math.copySign((Math.round(v / 2) * 2).toDouble, v)
  def copysign(v: Double, sign: Double): Double = Math.copySign(v, sign)

  def logNatural(v: Double): Double = Math.log(v)
