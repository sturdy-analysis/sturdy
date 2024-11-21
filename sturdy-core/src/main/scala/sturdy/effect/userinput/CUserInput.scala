package sturdy.effect.userinput

import sturdy.effect.Stateless

class CUserInput[A](nextInput: () => A) extends ImplicitlyNamedUserInput[A], Stateless:
  override def read(): A = nextInput()