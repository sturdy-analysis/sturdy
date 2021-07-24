package sturdy.values.strings

import sturdy.effect.failure.Failure

import scala.util.Random

trait StringOps[V](using Failure):
  def stringLit(s: String): V

given ConcreteStringOps(using f: Failure): StringOps[String] with
  def stringLit(s: String): String = s