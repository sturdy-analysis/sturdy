package sturdy.values.ints

import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureKind

import scala.util.Random

case object IntDivisionByZero extends FailureKind

trait IntOps[V](using Failure):
  def intLit(i: Int): V
  def randomInt(): V
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V

given ConcreteIntOps(using f: Failure): IntOps[Int] with
  def intLit(i: Int): Int = i
  def randomInt(): Int = Random.nextInt()
  def add(v1: Int, v2: Int): Int = v1 + v2
  def sub(v1: Int, v2: Int): Int = v1 - v2
  def mul(v1: Int, v2: Int): Int = v1 * v2
  def div(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
    else
      v1 / v2
