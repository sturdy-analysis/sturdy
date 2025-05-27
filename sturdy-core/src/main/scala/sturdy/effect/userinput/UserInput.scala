package sturdy.effect.userinput

import sturdy.effect.Effect

/** [[UserInput]] describes the effect of reading user input from the console. */
trait UserInput[A] extends Effect:
  def read(): A
