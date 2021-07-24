package sturdy.values.rational

import sturdy.effect.failure.Failure

import scala.util.Random

trait RationalOps[V](using Failure):
  def rationalLit(i1: Int, i2: Int): V

given ConcreteRationalOps(using f: Failure): RationalOps[(Int,Int)] with
  def rationalLit(i1: Int, i2: Int): (Int, Int) = (i1, i2)
