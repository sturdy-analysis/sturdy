package sturdy.values.abstraction.symbolic

import sturdy.values.{Combine, MaybeChanged, Widening}

/** replace this by Apron ASTs, may not capture all our operations (see `div` below) */
enum IntExp[S]:
  case Symbol(s: S)
  case IntLit(i: Int)
  case Rand()
  case Add(e1: IntExp[S], e2: IntExp[S])
  case Sub(e1: IntExp[S], e2: IntExp[S])
  case Mul(e1: IntExp[S], e2: IntExp[S])

given CombineIntExp[B, S, W <: Widening](using sym: Symbolic[IntExp[S], S]): Combine[IntExp[S], W] with
  override def apply(v1: IntExp[S], v2: IntExp[S]): MaybeChanged[IntExp[S]] =
    ???

