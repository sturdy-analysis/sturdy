package sturdy.data

import sturdy.values.{Combine, MaybeChanged, Widening}

given CombineOption[A, W <: Widening](using combineA: Combine[A, W]): Combine[Option[A], W] = {
  case (Some(v1), Some(v2)) => combineA(v1, v2).map(Some(_))
  case (None,     Some(v2)) => MaybeChanged.Changed(Some(v2))
  case (_,        None)     => MaybeChanged.Unchanged(None)
}