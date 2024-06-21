package sturdy.effect.userinput

import sturdy.effect.Stateless

abstract class CUserInput[A](nextInput: () => A) extends UserInput[A], Stateless:
  override def read(): A = nextInput()
