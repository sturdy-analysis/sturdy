package sturdy.apron

import apron.{Abstract1, Interval, Texpr1CstNode, Texpr1Node, Texpr1VarNode}

import java.util.Objects

object ApronVar:
  val DEBUG: Boolean = true
  private var instCount: Int = 0

trait ApronVar:
  private var freed: Boolean = false
  private var _delegate: ApronExpr = _
  def delegate: ApronExpr =
    if (_delegate != null)
      _delegate
    else
      ApronExpr.Var(this)

  val av: apron.Var
  val isInt: Boolean
  private val instCount = {
    val c = ApronVar.instCount
    ApronVar.instCount += 1
    c
  }

  private def internalName: String =
    val instDesc =
      if (ApronVar.DEBUG)
        instCount
      else
        Integer.toHexString(System.identityHashCode(this))
    s"$av#$instDesc}"

  def getBound(apron: Apron): Interval =
    if (freed)
      apron.getBound(_delegate)
    else {
      apron.getAVBound(av)
    }

  def ensureInitialized(apronState: Abstract1): Unit =
    if (!isInitialized(apronState))
      initialize(apronState)

  def isInitialized(apronState: Abstract1): Boolean =
    apronState.getEnvironment.hasVar(av)

  def initialize(apronState: Abstract1): Unit =
    if (!freed) {
      val intAr = if (isInt) Array(av) else null
      val floAr = if (isInt) null else Array(av)
      apronState.changeEnvironment(apronState.getCreationManager, apronState.getEnvironment.add(intAr, floAr), false)
      if (Apron.debugAlloc)
        println(s"Initializing $this = [-oo,+oo]")
    }

  def isOpen: Boolean =
    !freed

  def isDelegated: Boolean =
    freed && _delegate != null

  def isFrozen: Boolean =
    freed && _delegate == null

  def free(apron: Apron): Unit =
    setDelegate(ApronExpr.Constant(apron.getAVBound(av)))

  def setDelegate(delegate: ApronExpr): Unit =
    if (freed)
      throw new IllegalStateException(s"Cannot delegate freed variable $this")
    this._delegate = delegate
    freed = true
    if (Apron.debugAssert) {
      println(s"Delegating $internalName = $_delegate")
    }

  def frozen(): this.type =
    if (freed)
      throw new IllegalStateException(s"Cannot freeze freed variable $this")
    freed = true
    this

  def expr: ApronExpr =
    if (_delegate != null)
      _delegate
    else
      ApronExpr.Var(this)
  def node: Texpr1Node =
    if (_delegate != null)
      _delegate.toApron
    else
      new Texpr1VarNode(av)

  override def toString: String =
    if (_delegate != null)
      s"$_delegate (freed $internalName)"
    else if (freed)
      s"$av"
    else
      internalName

  override def equals(obj: Any): Boolean = obj match
    case that: ApronVar =>
      val sameAV = this.av == that.av
      val bothFreed = this.freed == that.freed
      val sameDelegate = this._delegate == that._delegate
      val bool = sameAV && bothFreed && sameDelegate
//      println(s"$bool:   $this == $obj")
      bool
    case _ => false

  override def hashCode(): Int =
    av.hashCode()
