package sturdy.values.symbols

import sturdy.effect.failure.Failure

import scala.util.Random

trait SymbolOps[V](using Failure):
  def symbolLit(s: String): V

given ConcreteSymbolOps(using f: Failure): SymbolOps[String] with
  def symbolLit(s: String): String = s