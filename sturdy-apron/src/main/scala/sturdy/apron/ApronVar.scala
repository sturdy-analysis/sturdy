package sturdy.apron

import apron.{Abstract1, Interval, Texpr1CstNode, Texpr1Node, Texpr1VarNode}

import java.util.Objects

object ApronVar:
  val DEBUG: Boolean = true
  private var instCount: Long = 0
  type UID = (apron.Var, Long)


trait ApronVar:
  val av: apron.Var
  val isInt: Boolean
  val ap: Apron

  private val instCount: Long = {
    val c = ApronVar.instCount
    ApronVar.instCount += 1
    c
  }
  
  def uid: ApronVar.UID = (av, instCount)

//  def initialize(apronState: Abstract1): Unit =
//    if (!freed) {
//      val intAr = if (isInt) Array(av) else null
//      val floAr = if (isInt) null else Array(av)
//      apronState.changeEnvironment(apronState.getCreationManager, apronState.getEnvironment.add(intAr, floAr), false)
//      if (Apron.debugAlloc)
//        println(s"Initializing $this = [-oo,+oo]")
//    }


  def expr: ApronExpr = ApronExpr.Var(this)
  def node: Texpr1Node = new Texpr1VarNode(av)

  override def toString: String =
    if (!Apron.debugAlloc)
      s"$av#$instCount"
    else
      ap.getFreedReference(this) match
        case None => s"$av#$instCount"
        case Some(e) => s"$av#$instCount=${ap.getBound(e)}"

  override def equals(obj: Any): Boolean = obj match
    case that: ApronVar =>
      (ap.getFreedReference(this), ap.getFreedReference(that)) match
        case (None, None) => this.instCount == that.instCount
        case (Some(e1), Some(e2)) => e1 == e2
        case _ => false
    case _ => false

  override def hashCode(): Int =
    av.hashCode()
