package sturdy.apron

import apron.{Abstract1, Interval, Texpr1CstNode, Texpr1Node, Texpr1VarNode}

import java.util.Objects

object ApronVar:
  val DEBUG: Boolean = true
  private var instCount: Long = 0

trait ApronVar:
  val av: apron.Var
  val isInt: Boolean
  private val instCount: Long = {
    val c = ApronVar.instCount
    ApronVar.instCount += 1
    c
  }

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

  def toString(apron: Apron): String =
    if (apron.inScope(this))
      this.toString
    else
      apron.getBound(this).toString

  override def equals(obj: Any): Boolean = obj match
    case that: ApronVar =>
      this.instCount == that.instCount && this.av == that.av
    case _ => false

  override def hashCode(): Int =
    av.hashCode() + instCount.toInt
