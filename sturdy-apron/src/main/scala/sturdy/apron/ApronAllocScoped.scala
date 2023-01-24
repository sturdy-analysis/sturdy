package sturdy.apron

import apron.*

class ApronAllocScoped(manager: Manager) extends ApronAlloc:
  enum Var extends ApronVar:
    case IntVar(local: String)(val ap: Apron)
    case DoubleVar(local: String)(val ap: Apron)
    case IntTemp(ix: Int)(val ap: Apron)
    case DoubleTemp(ix: Int)(val ap: Apron)

    val av: apron.Var = this match
      case IntVar(local) => new StringVar(s"I_$local")
      case DoubleVar(local) => new StringVar(s"D_$local")
      case IntTemp(ix) => new StringVar(s"I_temp_$ix")
      case DoubleTemp(ix) => new StringVar(s"D_temp_$ix")

    def copy: Var = this match
      case IntVar(local) => IntVar(local)(this.ap)
      case DoubleVar(local) => DoubleVar(local)(this.ap)
      case IntTemp(ix) => IntTemp(ix)(this.ap)
      case DoubleTemp(ix) => DoubleTemp(ix)(this.ap)

    override val isInt: Boolean = this match
      case _: IntVar | _: IntTemp => true
      case _ => false

  private var activeIntVars: Map[String, Var] = Map()
  private var doubleVarCount: Map[String, Int] = Map().withDefaultValue(0)
  private var intTempCount = 0
  private var doubleTempCount = 0

  def boundIntVars: Iterable[Var] = activeIntVars.values
  def boundDoubleVars: Iterable[Var] = ???

  override def toString: String =
    activeIntVars.toString()

  def allocateIntVariable(site: ApronAllocationSite, apron: Apron): Var = site match
    case ApronAllocationSite.TemporaryVar =>
      val v = Var.IntTemp(intTempCount)(apron)
      intTempCount += 1
      if (Apron.debugAlloc)
        println(s"allocating strong $v for $site")
      v
    case ApronAllocationSite.LocalVar(local) =>
      val v = Var.IntVar(local)(apron)
      activeIntVars.get(local) match
        case Some(active) =>
          if (Apron.debugAlloc)
            println(s"reallocating $active -> $v for $site")
          apron.freeVariable(active.asInstanceOf[apron.alloc.Var])
          if (Apron.debugAlloc && activeIntVars.contains(local))
            throw new IllegalStateException(s"$local should have been freed before reallocating it")
        case None =>
          if (Apron.debugAlloc)
            println(s"allocating strong $v for $site")
      activeIntVars += local -> v
      v

  def allocateDoubleVariable(site: ApronAllocationSite, apron: Apron): Var =
    ???

  override def freeVariable(v: Var, apron: Apron): Boolean = v match
    case v: Var.IntVar =>
      activeIntVars -= v.local
      true
    case v: Var.DoubleVar =>
      ???
    case _: Var.IntTemp | _: Var.DoubleTemp => true

  override def useStrongUpdate(v: Var): Boolean = true

  override def freshReference(v: Var): Var = v match
    case v: Var.IntVar =>
      val newV = v.copy
      if (Apron.debugAlloc)
        println(s"fresh reference box $newV")
      activeIntVars += v.local -> newV
      newV
    case v: Var.DoubleVar =>
      ???
    case _ =>
      val newV = v.copy
      if (Apron.debugAlloc)
        println(s"fresh reference box $newV")
      newV

