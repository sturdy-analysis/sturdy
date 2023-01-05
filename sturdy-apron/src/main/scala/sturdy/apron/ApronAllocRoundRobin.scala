package sturdy.apron

import apron.Texpr1VarNode
import apron.{Environment, Var, StringVar, Dimchange, Manager, Abstract1}

class ApronAllocRoundRobin(manager: Manager, varCountLimit: Int = 3) extends ApronAlloc:
  class Var(val av: apron.Var, val isInt: Boolean) extends ApronVar {
    def copy: Var =
      val newV = new Var(av, isInt)
      newV._refCount = refCount + 1
      newV
  }

  private var varCount: Int = 0

  val STRONG_UPDATE_SUFFIX = "$STRONG"

  def allocateIntVariable(site: ApronAllocationSite): Var =
    var cname = s"I${site}_$varCount"
    if (site == ApronAllocationSite.TemporaryVar)
      cname += STRONG_UPDATE_SUFFIX
    val v: apron.Var = new StringVar(cname)
    new Var(v, true)

  def allocateDoubleVariable(site: ApronAllocationSite): Var =
    var cname = s"D${site}_$varCount"
    if (site == ApronAllocationSite.TemporaryVar)
      cname += STRONG_UPDATE_SUFFIX
    val v: apron.Var = new StringVar(cname)
    new Var(v, false)

  override def freeVariable(v: Var, apron: Apron): Boolean =
    v.free(apron)
    useStrongUpdate(v)

  override def useStrongUpdate(v: Var): Boolean =
    !v.isDelegated && v.av.toString.endsWith(STRONG_UPDATE_SUFFIX)

  override def freshReference(v: Var): Var = v.copy

  override def frozenReference(v: Var): Var = v.copy.frozen()
