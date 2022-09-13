package sturdy.apron

import apron.Texpr1VarNode
import apron.{Environment, Var, StringVar, Dimchange, Manager, Abstract1}

class ApronAllocRoundRobin(manager: Manager, varCountLimit: Int = 3) extends ApronAlloc:
  case class ApronVar(protected val av: apron.Var) extends ApronVarOps

  private var varCount: Int = 0

  val STRONG_UPDATE_SUFFIX = "$STRONG"

  def addDoubleVariable(name: String, state: Abstract1, site: ApronAllocationSite): ApronVar =
    var cname = s"D${name}_$varCount"
    if (site == ApronAllocationSite.TemporaryVar)
      cname += STRONG_UPDATE_SUFFIX
    val v = new StringVar(cname)
    val env = state.getEnvironment
    if (!env.hasVar(v)) {
      varCount = (varCount + 1) % varCountLimit
      state.changeEnvironment(manager, env.add(null, Array[Var](v)), false)
    }
    ApronVar(v)

  def addIntVariable(name: String, state: Abstract1, site: ApronAllocationSite): ApronVar =
    var cname = s"I${name}_$varCount"
    if (site == ApronAllocationSite.TemporaryVar)
      cname += STRONG_UPDATE_SUFFIX
    val v = new StringVar(cname)
    val env = state.getEnvironment
    if (!env.hasVar(v)) {
      varCount = (varCount + 1) % varCountLimit
      state.changeEnvironment(manager, env.add(Array[Var](v), null), false)
    }
    ApronVar(v)

  override def freeVariable(v: ApronVar, state: Abstract1): Unit =
    if (useStrongUpdate(v)) {
      val av = v.getOrElse(throw new IllegalStateException(s"Cannot free variable $v, already freed"))
      state.forget(manager, av, false)
      val newEnv = state.getEnvironment.remove(Array(av))
      state.changeEnvironment(manager, newEnv, false)
    } else {
      // nothing
    }

  override def useStrongUpdate(v: ApronVar): Boolean =
    v.getOrElse(new StringVar("")).toString.endsWith(STRONG_UPDATE_SUFFIX)
