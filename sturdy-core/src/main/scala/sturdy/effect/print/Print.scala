package sturdy.effect.print

import sturdy.effect.Effect

/** [[Print]] describes the effect of printing to a console. */
trait Print[A] extends Effect:
  def apply(a: A): Unit
  def print(a: A): Unit = apply(a)

trait BackwardPrint[A] extends Effect:
  def apply(a: A => A): Unit
  def print(a: A => A): Unit = apply(a)
  
