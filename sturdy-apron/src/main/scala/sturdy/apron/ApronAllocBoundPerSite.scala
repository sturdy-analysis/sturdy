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

    val av: apron.Var = this match
      case IntVar(local) => new StringVar(s"I_$local")
      case DoubleVar(local) => new StringVar(s"D_$local")
      case IntTemp(ix) => new StringVar(s"I_temp_$ix")
      case DoubleTemp(ix) => new StringVar(s"D_temp_$ix")

    def copy: Var = this match
      case IntVar(local) => IntVar(local)
      case DoubleVar(local) => DoubleVar(local)
      case IntTemp(ix) => IntTemp(ix)
      case DoubleTemp(ix) => DoubleTemp(ix)

    override val isInt: Boolean = this match
      case _: IntVar | _: IntTemp => true
      case _ => false

  private var intVarCount: Map[String, (Int, Var)] = Map().withDefaultValue((0, null))
  private var doubleVarCount: Map[String, Int] = Map().withDefaultValue(0)
  private var intTempCount = 0
  private var doubleTempCount = 0

  def allocateIntVariable(site: ApronAllocationSite): Var =
    val (v, isStrong) = site match
      case ApronAllocationSite.TemporaryVar =>
        val x = Var.IntTemp(intTempCount)
        intTempCount += 1
        (x, true)
      case ApronAllocationSite.LocalVar(local) =>
        val (oldCount, oldX) = intVarCount(local)
        val x = if (oldCount == 0) {
          Var.IntVar(local)
        } else {
          oldX
        }
        intVarCount += local -> (oldCount + 1, x)
        (x, oldCount == 0)


    if (Apron.debugAlloc)
      println(s"allocating ${if (isStrong) "strong" else "weak"} $v")
    v

  def allocateDoubleVariable(site: ApronAllocationSite): Var =
    val (v, isStrong) = site match
      case ApronAllocationSite.TemporaryVar =>
        val x = Var.DoubleTemp(doubleTempCount)
        doubleTempCount += 1
        (x, true)
      case ApronAllocationSite.LocalVar(local) =>
        val oldCount = doubleVarCount(local)
        doubleVarCount += local -> (oldCount + 1)
        val x = Var.DoubleVar(local)
        (x, oldCount == 0)

    if (Apron.debugAlloc)
      println(s"allocating ${if (isStrong) "strong" else "weak"} $v")
    v

  override def freeVariable(v: Var, apron: Apron): Boolean =
    val isStrong = v match
      case v: Var.IntVar =>
        val (count, x) = intVarCount(v.local)
        intVarCount += v.local -> (count - 1, x)
        count == 1
      case v: Var.DoubleVar =>
        val count = doubleVarCount(v.local)
        doubleVarCount += v.local -> (count - 1)
        count == 1
      case _: Var.IntTemp | _: Var.DoubleTemp => true

    isStrong

  override def useStrongUpdate(v: Var): Boolean = v match
    case v: Var.IntVar => intVarCount(v.local)._1 == 1
    case v: Var.DoubleVar => doubleVarCount(v.local) == 1
    case _: Var.IntTemp | _: Var.DoubleTemp => true

  override def freshReference(v: Var): Var = v match
    case v: Var.IntVar =>
      val (count, x) = intVarCount(v.local)
      if (count <= 1) {
        val newV = v.copy
        if (Apron.debugAlloc)
          println(s"fresh reference box $newV")
        intVarCount += v.local -> (count, newV)
        newV
      } else {
        if (Apron.debugAlloc)
          println(s"reuse weak reference box $x")
        x
      }
    case _ =>
      val newV = v.copy
      if (Apron.debugAlloc)
        println(s"fresh reference box $newV")
      newV

