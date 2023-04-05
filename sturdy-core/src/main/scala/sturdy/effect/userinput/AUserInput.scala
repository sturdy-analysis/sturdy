package sturdy.effect.userinput

import sturdy.effect.Stateless

class AUserInput[A](approx: A) extends UserInput[A], Stateless:
  override def read(): A = approx
