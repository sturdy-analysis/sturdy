package sturdy.effect.print

import sturdy.effect.Effect

trait Assert[A] extends Effect:
  def apply(a: A): Unit
