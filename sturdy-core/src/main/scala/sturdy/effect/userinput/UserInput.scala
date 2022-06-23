package sturdy.effect.userinput

import sturdy.effect.Effect

trait UserInput[A] extends Effect:
  def read(): A
