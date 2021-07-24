package sturdy.values.ints

import sturdy.effect.failure.Failure

import scala.util.Random

trait IntOps[V](using Failure):
  def intLit(i: Int): V
  def abs(i: V): V
  def floor(i: V): V
  def ceiling(i: V): V
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
  
given ConcreteIntOps(using f: Failure): IntOps[Int] with
  def intLit(i: Int): Int = i
  def abs(v1: Int): Int = v1.abs
  def floor(v1: Int): Int = v1
  def ceiling(v1: Int): Int = v1
  def quotient(v1: Int, v2: Int): Int = div(v1,v2)
  def remainder(v1: Int, v2: Int): Int = abs(v1 % v2) // might be wrong
  def modulo(v1: Int, v2: Int): Int = v1 % v2
  def max(v1: Int, v2: Int): Int = v1.max(v2)
  def min(v1: Int, v2: Int): Int = v1.min(v2)
  def add(v1: Int, v2: Int): Int = v1 + v2
  def mul(v1: Int, v2: Int): Int = v1 * v2
  def sub(v1: Int, v2: Int): Int = v1 - v2
  def div(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(s"Division by zero: $v1 / $v2")
    else
      v1 / v2
  def gcd(v1: Int, v2: Int): Int = if (v1 == 0) v1.abs else gcd(v1, v1%v2)
  def lcm(v1: Int, v2: Int): Int = (v1 * v2).abs / gcd(v1,v2)