package sturdy.apron

import apron.*

class ApronAllocScoped(manager: Manager) extends ApronAlloc:
  enum Var extends ApronVar:
    case IntVar(local: String)
    case DoubleVar(local: String)
    case IntCallReturn(site: Any)
    case DoubleCallReturn(site: Any)
    case IntTemp(ix: Int)
    case DoubleTemp(ix: Int)

    val av: apron.Var = this match
      case IntVar(local) => new StringVar(s"I_$local")
      case DoubleVar(local) => new StringVar(s"D_$local")
      case IntCallReturn(site) => new StringVar(s"I_ret_$site")
      case DoubleCallReturn(site) => new StringVar(s"D_ret_$site")
      case IntTemp(ix) => new StringVar(s"I_temp_$ix")
      case DoubleTemp(ix) => new StringVar(s"D_temp_$ix")

    def copy: Var = this match
      case IntVar(local) => IntVar(local)
      case DoubleVar(local) => DoubleVar(local)
      case IntCallReturn(name) => IntCallReturn(name)
      case DoubleCallReturn(name) => DoubleCallReturn(name)
      case IntTemp(ix) => IntTemp(ix)
      case DoubleTemp(ix) => DoubleTemp(ix)

    override val isInt: Boolean = this match
      case _: IntVar | _: IntTemp | _: IntCallReturn => true
      case _ => false

  private var intTempCount = 0
  private var activeIntVars: Map[String, Var] = Map()
  private var intCallReturnCount: Map[Any, (Int, Var)] = Map().withDefaultValue((0, null.asInstanceOf[Var]))

  def boundIntVars: Iterable[Var] = activeIntVars.values
  def boundDoubleVars: Iterable[Var] = ???

  override def toString: String =
    activeIntVars.toString()

  def allocateIntVariable(site: ApronAllocationSite, apron: Apron): Var = site match
    case ApronAllocationSite.TemporaryVar =>
      val v = Var.IntTemp(intTempCount)
      intTempCount += 1
      if (Apron.debugAlloc)
        println(s"allocating strong $v for $site")
      v
    case ApronAllocationSite.LocalVar(local) =>
      val v = Var.IntVar(local)
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
    case ApronAllocationSite.CallReturnVar(site) =>
      val (count, oldV) = intCallReturnCount(site)
      if (count >= 1) {
        if (Apron.debugAlloc)
          println(s"allocating weak $oldV for $site")
        intCallReturnCount += site -> (count + 1, oldV)
        oldV
      } else {
        val v = Var.IntCallReturn(site)
        if (Apron.debugAlloc)
          println(s"allocating strong $v for $site")
        intCallReturnCount += site -> (1, v)
        v
      }

  def allocateDoubleVariable(site: ApronAllocationSite, apron: Apron): Var =
    ???

  override def freeVariable(v: Var, apron: Apron): Boolean = v match
    case _: Var.IntTemp | _: Var.DoubleTemp => true
    case Var.IntVar(local) =>
      activeIntVars -= local
      true
    case v: Var.DoubleVar =>
      ???
    case Var.IntCallReturn(site) =>
      val (count, x) = intCallReturnCount(site)
      intCallReturnCount += site -> (count - 1, x)
      // TODO count == 1
      false
    case Var.DoubleCallReturn(site) =>
      ???


  override def useStrongUpdate(v: Var): Boolean = v match
    case _: (Var.IntTemp | Var.DoubleTemp | Var.IntVar | Var.DoubleVar) => true
    case Var.IntCallReturn(site) => false // TODO intCallReturnCount(site)._1 == 1
    case Var.DoubleCallReturn(site) => ???

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

