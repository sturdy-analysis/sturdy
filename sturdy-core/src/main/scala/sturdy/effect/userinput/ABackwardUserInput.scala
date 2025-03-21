package sturdy.effect.userinput

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.Monotone
import sturdy.values.{*, given}

class ABackwardUserInput[A](using Finite[A]) extends BackwardUserInput[A], Monotone:
  protected var symbols: Set[A] = Set()
  
  override def read: A => A = a =>
    symbols += a
    a
    

