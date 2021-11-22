package sturdy.effect.print

import sturdy.effect.Effectful

trait Print[A] extends Effectful:
  def apply(a: A): Unit
