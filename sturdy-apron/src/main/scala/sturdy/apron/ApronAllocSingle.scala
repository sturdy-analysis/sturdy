package sturdy.apron

import apron.{Manager, StringVar}

class ApronAllocSingle(manager: Manager) extends ApronAlloc:
  enum Var extends ApronVar:
    case IntVar()(val ap: Apron)
    case DoubleVar()(val ap: Apron)
    case IntTemp(ix: Int)(val ap: Apron)
    case DoubleTemp(ix: Int)(val ap: Apron)

    val av: apron.Var = this match
      case IntVar() => new StringVar(s"I")
      case DoubleVar() => new StringVar(s"D")
      case IntTemp(ix) => new StringVar(s"I_temp_$ix")
      case DoubleTemp(ix) => new StringVar(s"D_temp_$ix")

    def copy: Var = this match
      case IntVar() => IntVar()(this.ap)
      case DoubleVar() => DoubleVar()(this.ap)
      case IntTemp(ix) => IntTemp(ix)(this.ap)
      case DoubleTemp(ix) => DoubleTemp(ix)(this.ap)

    override val isInt: Boolean = this match
      case _: IntVar | _: IntTemp => true
      case _ => false

  private var intVarCount = 0
  private var doubleVarCount = 0
  private var intTempCount = 0
  private var doubleTempCount = 0

  def boundIntVars: Iterable[Var] = ???
  def boundDoubleVars: Iterable[Var] = ???

  def allocateIntVariable(site: ApronAllocationSite, apron: Apron): Var =
    val (v, isStrong) = site match
      case ApronAllocationSite.TemporaryVar =>
        val x = Var.IntTemp(intTempCount)(apron)
        intTempCount += 1
        (x, true)
      case ApronAllocationSite.LocalVar(_) =>
        intVarCount += 1
        (Var.IntVar()(apron), intVarCount == 1)


    if (Apron.debugAlloc)
      println(s"allocating ${if (isStrong) "strong" else "weak"} $v for $site")
    v

  def allocateDoubleVariable(site: ApronAllocationSite, apron: Apron): Var =
    val (v, isStrong) = site match
      case ApronAllocationSite.TemporaryVar =>
        val x = Var.DoubleTemp(doubleTempCount)(apron)
        doubleTempCount += 1
        (x, true)
      case ApronAllocationSite.LocalVar(_) =>
        doubleVarCount += 1
        (Var.DoubleVar()(apron), doubleVarCount == 1)

    if (Apron.debugAlloc)
      println(s"allocating ${if (isStrong) "strong" else "weak"} $v for $site")
    v

  override def freeVariable(v: Var, apron: Apron): Boolean =
    v match
      case _: Var.IntVar =>
        intVarCount -= 1
        intVarCount == 0
      case _: Var.DoubleVar =>
        doubleVarCount -= 1
        doubleVarCount == 0
      case _: Var.IntTemp | _: Var.DoubleTemp => true

  override def useStrongUpdate(v: Var): Boolean = v match
    case _: Var.IntVar | _: Var.DoubleVar => false
    case _: Var.IntTemp | _: Var.DoubleTemp => true

  override def freshReference(v: Var): Var =
    val newV = v.copy
    if (Apron.debugAlloc)
      println(s"fresh reference box $newV")
    newV

