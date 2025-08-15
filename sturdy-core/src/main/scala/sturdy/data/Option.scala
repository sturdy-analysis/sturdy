package sturdy.data

import sturdy.{IsSound, Soundness}
import sturdy.values.{Abstractly, Combine, MaybeChanged, Widening}

given CombineOption[A, W <: Widening](using combineA: Combine[A, W]): Combine[Option[A], W] = {
  case (Some(v1), Some(v2))   => combineA(v1, v2).map(Some(_))
  case (None,     v2@Some(_)) => MaybeChanged.Changed(v2)
  case (v1,       None)       => MaybeChanged.Unchanged(v1)
}

given SoundnessOption[C,A](using Soundness[C,A]): Soundness[Option[C], Option[A]] = {
  case (None, None)        => IsSound.Sound
  case (None, Some(_))     => IsSound.Sound
  case (Some(c), Some(a))  => Soundness.isSound(c,a)
  case (c@Some(_), a@None) => IsSound.NotSound(s"Concrete option $c does not overapproximate abstract option $a")
}