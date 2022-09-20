package sturdy.apron

import apron.Texpr1VarNode
import apron.Abstract1
import apron.Dimchange
import apron.Environment
import apron.Manager
import apron.StringVar
import apron.Var

class ApronAllocBoundPerSite(manager: Manager) extends ApronAlloc:
  enum Var extends ApronVar:
    case IntVar(local: String)
    case DoubleVar(local: String)
    case IntTemp(ix: Int)
    case DoubleTemp(ix: Int)

    private[apron] def _av: apron.Var = av

    protected val av: apron.Var = this match
      case IntVar(local) => new StringVar(s"I_$local")
      case DoubleVar(local) => new StringVar(s"D_$local")
      case IntTemp(ix) => new StringVar(s"I_temp_$ix")
      case DoubleTemp(ix) => new StringVar(s"D_temp_$ix")

    override val isInt: Boolean = this match
      case _: IntVar | _: IntTemp => true
      case _ => false

  private var varCount: Map[Var, Int] = Map().withDefaultValue(0)
  private var intTempCount = 0
  private var doubleTempCount = 0

  def addIntVariable(state: Abstract1, site: ApronAllocationSite): Var =
    val (v, isStrong) = site match
      case ApronAllocationSite.TemporaryVar =>
        val x = Var.IntTemp(intTempCount)
        intTempCount += 1
        (x, true)
      case ApronAllocationSite.LocalVar(local) =>
        val x = Var.IntVar(local)
        val oldCount = varCount(x)
        varCount += x -> (oldCount + 1)
        (x, oldCount == 0)

    if (Apron.debugAlloc)
      println(s"allocating ${if (isStrong) "strong" else "weak"} $v")
    v

  def addDoubleVariable(state: Abstract1, site: ApronAllocationSite): Var =
    val (v, isStrong) = site match
      case ApronAllocationSite.TemporaryVar =>
        val x = Var.DoubleTemp(doubleTempCount)
        doubleTempCount += 1
        (x, true)
      case ApronAllocationSite.LocalVar(local) =>
        val x = Var.DoubleVar(local)
        val oldCount = varCount(x)
        varCount += x -> (oldCount + 1)
        (x, oldCount == 0)

    if (Apron.debugAlloc)
      println(s"allocating ${if (isStrong) "strong" else "weak"} $v")
    v

  override def freeVariable(v: Var, state: Abstract1): Unit =
    val isStrong = v match
      case _: Var.IntVar | _: Var.DoubleVar =>
        val count = varCount(v)
        varCount += v -> (count - 1)
        count == 1
      case _: Var.IntTemp | _: Var.DoubleTemp => true

    v.free(state)

    if (Apron.debugAlloc)
      println(s"freeing ${if (isStrong) "strong" else "weak"} $v")

    if (isStrong && v.isInitialized(state)) {
      // TODO forget obsolete if removing dimension?
      state.forget(manager, v._av, false)
      val newEnv = state.getEnvironment.remove(Array(v._av))
      state.changeEnvironment(manager, newEnv, false)
    } else {
      // nothing
    }

  override def useStrongUpdate(v: Var): Boolean = v match
    case _: Var.IntVar | _: Var.DoubleVar => varCount(v) == 1
    case _: Var.IntTemp | _: Var.DoubleTemp => true

