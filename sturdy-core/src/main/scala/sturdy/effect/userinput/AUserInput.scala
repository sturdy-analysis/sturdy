package sturdy.effect.userinput

import sturdy.effect.Stateless

class AUserInput[A](approx: A) extends UserInput[A], Stateless:
  override def read(): A = approx
  override def readField(name: String): A = approx

abstract class AUserInputFun[A](approx: => A) extends UserInput[A], Stateless:
  override def read(): A = approx
  private var fields: Map[String, A] = Map()

trait WithNamedUserInput[A](namedInput: String => A) extends UserInput[A]:
  final override def readField(name: String): A = namedInput(name)