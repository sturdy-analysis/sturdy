package sturdy.apron

import apron.*

import java.util.Objects

object ApronVar:
  val DEBUG: Boolean = true
  private var instCount: Long = 0
  type UID = ApronVar


trait ApronVar:
  val av: apron.Var
  val isInt: Boolean

  private val instCount: Long = {
    val c = ApronVar.instCount
    ApronVar.instCount += 1
    c
  }
  
  def uid: ApronVar.UID = this

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
    s"$av#$instCount"

  def toString(scope: ApronScope): String =
    if (!Apron.debugAlloc)
      s"$av#$instCount"
    else
      scope.getFreedReference(this) match
        case None => s"$av#$instCount~${scope.getBound(this)}"
        case Some(e) => s"$av#$instCount=${scope.getBound(e)}"

  override def equals(obj: Any): Boolean = obj match
    case that: ApronVar => this.instCount == that.instCount
    case _ => false

  def isEqual(that: ApronVar, scope: ApronScope): Boolean = (scope.getFreedReference(this), scope.getFreedReference(that)) match
      case (None, None) => this.instCount == that.instCount
      case (Some(e1), Some(e2)) => e1 == e2
      case _ => false

  override def hashCode(): Int =
    av.hashCode()

  def hashCode(scope: ApronScope): Int = scope.getFreedReference(this) match
    case None => this.instCount.hashCode()
    case Some(e) => e.hashCode(scope)


