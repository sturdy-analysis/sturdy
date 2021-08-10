package sturdy.effect.userinput

trait CUserInput[A](nextInput: () => A) extends UserInput[A]:
  override def readInput() = nextInput()
