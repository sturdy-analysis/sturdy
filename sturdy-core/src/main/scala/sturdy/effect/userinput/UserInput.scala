package sturdy.effect.userinput

import sturdy.effect.Effect

/** [[UserInput]] describes the effect of reading user input from the console. */
trait UserInput[A] extends Effect:
  /** Reads a value non-deterministically: each invocation produces a new, unrelated value. */
  def read(): A
  /** Reads a value deterministically: each invocation for `name` yields the same value */
  def readField(name: String): A

trait ImplicitlyNamedUserInput[A] extends UserInput[A]:
  private var fields: Map[String, A] = Map()
  final override def readField(name: String): A = fields.get(name) match
    case Some(a) => a
    case None =>
      val a = read()
      fields += name -> a
      a
