package sturdy.effect.userinput

trait AUserInput[A](approx: A) extends UserInput[A]:
  override def readInput() = approx
