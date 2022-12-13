package sturdy.effect.assert

import sturdy.effect.Effect

// add context in apply
trait Assert[A] extends Effect:
  def apply(a: A): Unit
