package sturdy.apron

import apron.{Abstract1, Dimchange, Environment, Manager, StringVar, Var}

class ApronAllocRoundRobin(manager: Manager, varCountLimit: Int = 10) extends ApronAlloc:
  private var varCount: Int = 0

  def addDoubleVariable(name: String, state: Abstract1): Var =
    val cname = s"D${name}_$varCount"
    val v = new StringVar(cname)
    val env = state.getEnvironment
    if (!env.hasVar(v)) {
      varCount = (varCount + 1) % varCountLimit
      state.changeEnvironment(manager, env.add(null, Array[Var](v)), false)
    }
    v

  def addIntVariable(name: String, state: Abstract1): Var =
    val cname = s"I${name}_$varCount"
    val v = new StringVar(cname)
    val env = state.getEnvironment
    if (!env.hasVar(v)) {
      varCount = (varCount + 1) % varCountLimit
      state.changeEnvironment(manager, env.add(Array[Var](v), null), false)
    }
    v
