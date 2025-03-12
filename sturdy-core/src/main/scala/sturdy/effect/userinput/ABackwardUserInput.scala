package sturdy.effect.userinput

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.Monotone
import sturdy.values.{*, given}

class ABackwardUserInput[A](using Finite[A]) extends BackwardUserInput[A], Monotone:

  override def read: A => A =
    ???

