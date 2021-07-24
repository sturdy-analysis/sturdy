package sturdy.values.quotes

import sturdy.effect.failure.Failure

class LiftedQuoteOps[L, V, D](extract: V => D, inject: D => V)(using ops: QuoteOps[L, D])(using Failure) extends QuoteOps[L, V]:
  def quoteLit(l: L): V = inject(ops.quoteLit(l))

