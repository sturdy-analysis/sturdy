package sturdy.effect.userinput

import sturdy.effect.Effectful

trait UserInput[A] extends Effectful:
  def read(): A
