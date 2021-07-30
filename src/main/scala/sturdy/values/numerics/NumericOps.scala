package sturdy.values.numerics

import scala.util.Random

trait NumericOps[V]:
  def isZero(v: V): V
  def isPositive(v: V): V
  def isNegative(v: V): V
  def isOdd(v: V): V
  def isEven(v: V): V
  def abs(v: V): V
  def floor(v: V): V
  def ceiling(v: V): V
  def log(v: V): V
  def quotient(v1: V, v2: V): V
  def remainder(v1: V, v2: V): V
  def modulo(v1: V, v2: V): V
  def max(v1: V, v2: V): V
  def min(v1: V, v2: V): V
  def add(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def div(v1: V, v2: V): V
  def gcd(v1: V, v2: V): V
  def lcm(v1: V, v2: V): V
