package sturdy.apron

import apron.Texpr1VarNode
import apron.Abstract1
import apron.Dimchange
import apron.Environment
import apron.Manager
import apron.StringVar
import apron.Var

class ApronAllocBoundPerSite(manager: Manager) extends ApronAlloc:
  enum ApronVar extends ApronVarOps:
    case IntVar(local: String)
    case DoubleVar(local: String)
    case IntTemp(ix: Int)
    case DoubleTemp(ix: Int)

    private[apron] def _av = av
    protected val av: Var = this match
      case IntVar(local) => new StringVar(s"I_$local")
      case DoubleVar(local) => new StringVar(s"D_$local")
      case IntTemp(ix) => new StringVar(s"I_temp_$ix")
      case DoubleTemp(ix) => new StringVar(s"D_temp_$ix")

  private var varCount: Map[ApronVar, Int] = Map().withDefaultValue(0)
  private var intTempCount = 0
  private var doubleTempCount = 0

  def addIntVariable(name: String, state: Abstract1, site: ApronAllocationSite): ApronVar =
    val v = site match
      case ApronAllocationSite.TemporaryVar =>
        val x = ApronVar.IntTemp(intTempCount)
        intTempCount += 1
        x
      case ApronAllocationSite.LocalVar(local) =>
        val x = ApronVar.IntVar(local)
        varCount += x -> (varCount(x) + 1)
        x

    if (!state.getEnvironment.hasVar(v._av)) {
      state.changeEnvironment(manager, state.getEnvironment.add(Array[Var](v._av), null), false)
    }
    v

  def addDoubleVariable(name: String, state: Abstract1, site: ApronAllocationSite): ApronVar =
    val v = site match
      case ApronAllocationSite.TemporaryVar =>
        val x = ApronVar.DoubleTemp(doubleTempCount)
        doubleTempCount += 1
        x
      case ApronAllocationSite.LocalVar(local) =>
        val x = ApronVar.DoubleVar(local)
        varCount += x -> (varCount(x) + 1)
        x

    if (!state.getEnvironment.hasVar(v._av)) {
      state.changeEnvironment(manager, state.getEnvironment.add(null, Array[Var](v.av)), false)
    }
    v

  override def freeVariable(v: ApronVar, state: Abstract1): Unit =
    val isStrong = v match
      case _: ApronVar.IntVar | _: ApronVar.DoubleVar =>
        val count = varCount(v)
        varCount += v -> (count - 1)
        count == 1
      case _: ApronVar.IntTemp | _: ApronVar.DoubleTemp => true

    if (isStrong) {
      state.forget(manager, v._av, false)
      val newEnv = state.getEnvironment.remove(Array(v.av))
      state.changeEnvironment(manager, newEnv, false)
    } else {
      // nothing
    }

  override def useStrongUpdate(v: ApronVar): Boolean = v match
    case _: ApronVar.IntVar | _: ApronVar.DoubleVar => varCount(v) == 1
    case _: ApronVar.IntTemp | _: ApronVar.DoubleTemp => true

