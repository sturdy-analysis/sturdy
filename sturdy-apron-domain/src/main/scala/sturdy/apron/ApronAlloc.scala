package sturdy.apron

import apron.{Abstract1, Environment, Var}

trait ApronAlloc:
  def addDoubleVariable(name: String, state: Abstract1): Var
  def addIntVariable(name: String, state: Abstract1): Var
