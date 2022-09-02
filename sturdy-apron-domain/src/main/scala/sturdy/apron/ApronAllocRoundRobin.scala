package sturdy.apron

import apron.{Abstract1, Dimchange, Environment, Manager, StringVar, Var}

class ApronAllocRoundRobin(manager: Manager, varCountLimit: Int = 3) extends ApronAlloc:
  private var varCount: Int = 0

  val STRONG_UPDATE_SUFFIX = "$STRONG"

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

  override def useStrongUpdate(v: Var): Boolean =
    v.toString.endsWith(STRONG_UPDATE_SUFFIX)
