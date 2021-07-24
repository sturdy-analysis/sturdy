package sturdy.values.chars

import sturdy.effect.failure.Failure

import scala.util.Random

trait CharOps[V](using Failure):
  def charLit(c: Char): V

given ConcreteCharOps(using f: Failure): CharOps[Char] with
  def charLit(c: Char): Char = c