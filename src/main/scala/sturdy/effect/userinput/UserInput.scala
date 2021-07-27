package sturdy.effect.userinput

trait UserInput[A]:
  def readInput(): A
