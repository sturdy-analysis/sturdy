package sturdy.values.relational

import sturdy.values.*
import sturdy.values.abstraction.symbolic.*

given SymbolicEqOps[S, V, B](using sym: SymbolicValue[IntExp[S], S, V]): EqOps[IntExp[S], Topped[Boolean]] with

  override def equ(v1: IntExp[S], v2: IntExp[S]): Topped[Boolean] = ???
  override def neq(v1: IntExp[S], v2: IntExp[S]): Topped[Boolean] = ???
