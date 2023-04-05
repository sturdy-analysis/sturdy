package sturdy.effect.print

import sturdy.effect.Effect

trait Print[A] extends Effect:
  def apply(a: A): Unit
