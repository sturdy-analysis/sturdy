package sturdy.values.relational

import sturdy.values.abstraction.symbolic.*
import sturdy.values.*

given SymbolicOrderingOps[S, V, B](using sym: SymbolicValue[IntExp[S], S, V]): OrderingOps[IntExp[S], Topped[Boolean]] with

  override def lt(v1: IntExp[S], v2: IntExp[S]): Topped[Boolean] = ???

  override def le(v1: IntExp[S], v2: IntExp[S]): Topped[Boolean] = ???
