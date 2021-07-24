package sturdy.values.quotes

import sturdy.effect.failure.Failure

import scala.util.Random

trait QuoteOps[L, V](using Failure):
  def quoteLit(l: L): V

given ConcreteQuoteOps[L, V](using f: Failure): QuoteOps[L, L] with
  def quoteLit(l: L): L = l