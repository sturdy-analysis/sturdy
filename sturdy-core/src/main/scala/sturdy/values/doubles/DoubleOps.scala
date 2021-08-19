package sturdy.values.doubles

import sturdy.effect.failure.Failure
import sturdy.values.config
import sturdy.values.convert.*

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

type ConvertDoubleInt[VFrom, VTo] = Convert[Double, Float, VFrom, VTo, (config.Overflow, config.Bits)]
type ConvertDoubleLong[VFrom, VTo] = Convert[Double, Long, VFrom, VTo, (config.Overflow, config.Bits)]
type ConvertDoubleFloat[VFrom, VTo] = Convert[Double, Float, VFrom, VTo, Unit]
