package sturdy.values.symbols

import sturdy.effect.failure.Failure

class LiftedSymbolOps[V, D](extract: V => D, inject: D => V)(using ops: SymbolOps[D])(using Failure) extends SymbolOps[V]:
  def symbolLit(s: String): V = inject(ops.symbolLit(s))

