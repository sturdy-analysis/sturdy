package sturdy.apron

import apron.Texpr1VarNode
import apron.{Environment, Var, StringVar, Dimchange, Manager, Abstract1}

class ApronAllocRoundRobin(manager: Manager, varCountLimit: Int = 3) extends ApronAlloc:
  case class Var(protected val av: apron.Var) extends ApronVar

  private var varCount: Int = 0

  val STRONG_UPDATE_SUFFIX = "$STRONG"

  def addDoubleVariable(state: Abstract1, site: ApronAllocationSite): Var =
    var cname = s"D${site}_$varCount"
    if (site == ApronAllocationSite.TemporaryVar)
      cname += STRONG_UPDATE_SUFFIX
    val v: apron.Var = new StringVar(cname)
    val env = state.getEnvironment
    if (!env.hasVar(v)) {
      varCount = (varCount + 1) % varCountLimit
      state.changeEnvironment(manager, env.add(null, Array(v)), false)
    }
    Var(v)

  def addIntVariable(state: Abstract1, site: ApronAllocationSite): Var =
    var cname = s"I${site}_$varCount"
    if (site == ApronAllocationSite.TemporaryVar)
      cname += STRONG_UPDATE_SUFFIX
    val v: apron.Var = new StringVar(cname)
    val env = state.getEnvironment
    if (!env.hasVar(v)) {
      varCount = (varCount + 1) % varCountLimit
      state.changeEnvironment(manager, env.add(Array(v), null), false)
    }
    Var(v)

  override def freeVariable(v: Var, state: Abstract1): Unit =
    if (useStrongUpdate(v)) {
      val av = v.getOrElse(throw new IllegalStateException(s"Cannot free variable $v, already freed"))
      state.forget(manager, av, false)
      val newEnv = state.getEnvironment.remove(Array(av))
      state.changeEnvironment(manager, newEnv, false)
    } else {
      // nothing
    }

  override def useStrongUpdate(v: Var): Boolean =
    v.getOrElse(new StringVar("")).toString.endsWith(STRONG_UPDATE_SUFFIX)
