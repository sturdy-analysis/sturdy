package sturdy.effect.print

import sturdy.effect.Effect

/** [[Print]] describes the effect of printing to a console. */
trait Print[A] extends Effect:
  def apply(a: A): Unit
