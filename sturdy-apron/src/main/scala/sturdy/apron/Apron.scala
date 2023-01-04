package sturdy.apron

import apron.{Texpr0UnNode, Environment, Texpr1CstNode, Texpr1UnNode, Interval, MpqScalar, Texpr1VarNode, Linexpr1, Texpr1Node, Tcons0, Tcons1, StringVar, Texpr1BinNode, Texpr1Intern, Manager, Abstract1}
import sturdy.apron.Apron.debugAssert
import sturdy.apron.Apron.debugJoinWiden
import sturdy.data.CombineUnit
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effect
import sturdy.effect.{EffectStack, SturdyFailure, CombineTrySturdy, TrySturdy}
import sturdy.values.{Combine, MaybeChanged, Widening, Topped, Join, Widen}

import java.lang
import java.lang.IllegalStateException
import scala.language.reflectiveCalls

object Apron:
  val debugAlloc: Boolean = true
  val debugAssign: Boolean = true
  val debugJoinWiden: Boolean = true
  val debugAssert: Boolean = true

  object Bottom extends SturdyFailure

class Apron(val apronManager: Manager, val alloc: ApronAlloc) extends Effect:
  override def toString: String =
    apronEnv.getVars.mkString("Array(", ", ", ") : ") + _apronState.toString(apronManager)

  private var _apronState: Abstract1 = new Abstract1(apronManager, new Environment())
  def apronState: Abstract1 = _apronState

  def apronEnv: Environment = _apronState.getEnvironment

  def getBound(v: alloc.Var): Interval =
    v.getBound(_apronState)

  def getBound(v: ApronExpr): Interval =
    getBound(v.toApron)

  def getBound(v: Texpr1Node): Interval =
    val vIntern = try {
      new Texpr1Intern(apronEnv, v)
    } catch {
      case e: IllegalArgumentException if e.getMessage == "no such variable" =>
        return null
    }
    _apronState.getBound(apronManager, vIntern)

  def addDoubleVariable(site: ApronAllocationSite): alloc.Var =
    alloc.addDoubleVariable(_apronState, site)

  def addIntVariable(site: ApronAllocationSite): alloc.Var =
    alloc.addIntVariable(_apronState, site)

  def freeVariable(v: alloc.Var): Unit =
    alloc.freeVariable(v, _apronState)

  def constrainPure[A](c: ApronCons)(a: A): Join[A] ?=> A = c.splitNeq match
    case Some((lt, gt)) =>
      ifThenElsePure(lt, gt, widen = false)(a)(a)
    case None =>
      assertConstrain(c)
      a

  def constrain[A](c: ApronCons)(a: => A)(using EffectStack): Join[A] ?=> A = c.splitNeq match
    case Some((lt, gt)) =>
      ifThenElse(lt, gt)(a)(a)
    case None =>
      assertConstrain(c)
      a

  def assertConstrain(ac: ApronCons): Unit =
    if (_apronState.isBottom(apronManager))
      throw new IllegalStateException(s"Apron state may not be bottom prior to constraining!")
    ac.vars.foreach(_.ensureInitialized(_apronState))
    val c = ac.toApron(apronEnv)
    if(c.getKind == Tcons1.DISEQ)
      throw new IllegalArgumentException("DISEQ constraints should be handled outside of the function!")
    c.extendEnvironment(apronEnv)
    _apronState.meet(apronManager, c)
    if (debugAssert)
      println(s"Asserting $ac yielding $this")
    if (_apronState.isBottom(apronManager))
      throw Apron.Bottom

  /** Assigns v := exp. In case of a strong assignment, invalidates previous references of v and yields a fresh copy of v to use for future references. */
  def assign(v: alloc.Var, exp: ApronExpr): Option[alloc.Var] =
    val av = v.getOrElse(throw new IllegalStateException(s"Cannot assign to freed variable $v"))
    val isStrong = alloc.useStrongUpdate(v)
    val isInitialized = v.isInitialized(_apronState)
    var newV: Option[alloc.Var] = None

    if (!isInitialized) {
      v.initialize(_apronState)
      val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
      if (Apron.debugAssign) {
        println(this)
        println(s"assigning uninitialized $v = $exp = ${_apronState.getBound(apronManager, expIntern)}, was uninitialized")
      }
      _apronState.assign(apronManager, v.av, expIntern, null)
    } else if (isStrong) {
      if (Apron.debugAlloc)
        println(s"freeing old references of $v = ${v.getBound(_apronState)}")
      v.free(_apronState)

      val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
      if (Apron.debugAssign) {
        val old = getBound(v)
        println(s"assigning strong $v = $exp = ${_apronState.getBound(apronManager, expIntern)}, was $old")
      }
      _apronState.assign(apronManager, av, expIntern, null)
      newV = Some(alloc.freshReference(v))
    } else {
      val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
      if (Apron.debugAssign) {
        val old = getBound(v)
        print(s"assigning weak $v = old join $exp = $old join ${_apronState.getBound(apronManager, expIntern)} = ")
      }
      val assigned = _apronState.assignCopy(apronManager, av, expIntern, null)
      _apronState.join(apronManager, assigned)
      if (Apron.debugAssign)
        println(getBound(v))
    }
    if (_apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")
    newV

  def withTemporaryIntVariable[A](f: alloc.Var => A): A =
    val v = alloc.addIntVariable(_apronState, ApronAllocationSite.TemporaryVar)
    val res = try f(v)
    finally {
      alloc.freeVariable(v, _apronState)
    }
    res

  def withTemporaryIntVariables[A](n: Int)(f: PartialFunction[List[alloc.Var], A]): A =
    val vs = (1 to n).toList.map(i => alloc.addIntVariable(_apronState, ApronAllocationSite.TemporaryVar))
    try f(vs)
    finally {
      vs.foreach(alloc.freeVariable(_, _apronState))
    }

  def withTemporaryDoubleVariable[A](f: alloc.Var => A): A =
    val v = alloc.addDoubleVariable(_apronState, ApronAllocationSite.TemporaryVar)
    try f(v)
    finally {
      alloc.freeVariable(v, _apronState)
    }

  def withTemporaryDoubleVariables[A](n: Int)(f: PartialFunction[List[alloc.Var], A]): A =
    val vs = (1 to n).toList.map(i => alloc.addDoubleVariable(_apronState, ApronAllocationSite.TemporaryVar))
    try f(vs)
    finally {
      vs.foreach(alloc.freeVariable(_, _apronState))
    }

  def ifThenElseUnit[A, B](cond: ApronCons)(ifTrue: => A)(ifFalse: => B)(using EffectStack): Unit =
    ifThenElse(cond, cond.negated)({ifTrue; ()})({ifFalse; ()})

  def ifThenElse[A](cond: ApronCons)(ifTrue: => A)(ifFalse: => A)(using EffectStack): Join[A] ?=> A =
    ifThenElse(cond, cond.negated)(ifTrue)(ifFalse)

  def ifThenElse[A](condTrue: ApronCons, condFalse: ApronCons)(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A =
    println(s"if ($condTrue) else if ($condFalse)")
    effects.joinComputations {
      constrain(condTrue)(ifTrue)
    } {
      constrain(condFalse)(ifFalse)
    }

  def ifThenElse[A](cond: Topped[ApronCons])(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A = cond match
    case Topped.Top => effects.joinComputations(ifTrue)(ifFalse)
    case Topped.Actual(b) => ifThenElse(b)(ifTrue)(ifFalse)

  def ifThenElsePure[A](condTrue: ApronCons, widen: Boolean)(ifTrue: A)(ifFalse: A): Join[A] ?=> A =
    ifThenElsePure(condTrue, condTrue.negated, widen)(ifTrue)(ifFalse)

  def ifThenElsePure[A](condTrue: ApronCons, condFalse: ApronCons, widen: Boolean)(ifTrue: A)(ifFalse: A): Join[A] ?=> A =
    println(s"if ($condTrue) else if ($condFalse) | pure")
    val snapshot = new Abstract1(apronManager, _apronState)
    val res1 = TrySturdy(constrainPure(condTrue)(ifTrue))
    val state1 = _apronState
    _apronState = new Abstract1(apronManager, snapshot)
    val res2 = TrySturdy(constrainPure(condFalse)(ifFalse))

    (res1.isBottom, res2.isBottom) match
      case (false, false) =>
        val state2 = _apronState
        _apronState = combineApronStates(state1, state2, widen).get
      case (false, true) =>
        _apronState = state1
      case (true, false) =>
        // nothing
      case (true, true) =>
        _apronState = snapshot

    if (_apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")
    Join(res1, res2).get.getOrThrow

  def combineApronStates(s1: Abstract1, s2: Abstract1, widen: Boolean): MaybeChanged[Abstract1] = {
    val vars1 = s1.getEnvironment.getVars.toSet
    val vars2 = s2.getEnvironment.getVars.toSet
    val inboth = (vars1 intersect vars2).toArray

    val lce = s1.getEnvironment.lce(s2.getEnvironment)
    val combinable1 = s1.changeEnvironmentCopy(apronManager, lce, false)
    val combinable2 = s2.changeEnvironmentCopy(apronManager, lce, false)
    val combined =
      if (widen)
        combinable1.widening(apronManager, combinable2)
      else
        combinable1.joinCopy(apronManager, combinable2)

    val s1Only = combinable1.forgetCopy(apronManager, inboth, false)
    val s2Only = combinable2.forgetCopy(apronManager, inboth, false)
    combined.meet(apronManager, s1Only)
    combined.meet(apronManager, s2Only)
    val changed = !combined.isEqual(apronManager, combinable1)
    if (debugJoinWiden) {
      println(
        s"""${if (widen) "Widening" else "Joining"} apron
           |  s1 = $s1
           |  s2 = $s2
           |  joined = $combined""".stripMargin)
    }
    if (debugJoinWiden && changed && combined.toString(apronManager) == combinable1.toString(apronManager))
      throw new IllegalStateException()
    if (_apronState.isBottom(apronManager))
      throw Apron.Bottom
    MaybeChanged(combined, changed)
  }

  def joinValues(e1: ApronExpr, e2: ApronExpr, widen: Boolean): MaybeChanged[ApronExpr] =
    if (e1 == e2) {
      MaybeChanged.Unchanged(e1)
    } else {
      withTemporaryIntVariable { x =>
        ifThenElsePure(
          ApronCons.eq(x.expr, e1),
          ApronCons.eq(x.expr, e2),
          widen)(())(())

        val xBound = getBound(x)
        val v1Bound = getBound(e1)
        MaybeChanged(x.expr, !xBound.isEqual(v1Bound))
      }
    }

  override type State = ApronState

  override def getState: ApronState =
    new ApronState(new Abstract1(apronManager, _apronState))
  override def setState(as: ApronState): Unit =
    val st = new Abstract1(apronManager, as.s)
//    setLeastExtendingEnvironment(st)
    val currentEnv = apronEnv
    _apronState = st
//    extendEnvironment(currentEnv)
    if (_apronState.isBottom(apronManager))
      throw new SturdyFailure {}

  override def join: Join[State] = (as1, as2) => {
    val s1 = as1.s
    val s2 = as2.s
    if (s1.isBottom(apronManager))
      MaybeChanged.Changed(as2)
    else if (s2.isBottom(apronManager))
      MaybeChanged.Unchanged(as1)
    else {
      val joined = combineApronStates(s1, s2, widen = false)
      val result = joined.map(new ApronState(_))
      result
    }
  }

  override def widen: Widen[State] = (as1, as2) => {
    val s1 = as1.s
    val s2 = as2.s
    if (s1.isBottom(apronManager))
      MaybeChanged.Changed(as2)
    else if (s2.isBottom(apronManager))
      MaybeChanged.Unchanged(as1)
    else {
      val widened = combineApronStates(s1, s2, widen = true)
      val result = widened.map(new ApronState(_))
      result
    }
  }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ApronComputationJoiner[A])
  
  class ApronComputationJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = new Abstract1(apronManager, _apronState)
    private var fState: Abstract1 = _

    override def inbetween(): Unit =
      fState = _apronState
      _apronState = new Abstract1(apronManager, snapshot)
//      extendEnvironment(fState.getEnvironment)

    override def retainNone(): Unit =
      _apronState = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      _apronState = fState

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      val gState = _apronState
      _apronState = combineApronStates(fState, gState, widen = false).get
  }

  class ApronState(val s: Abstract1):
    override def equals(obj: Any): Boolean = obj match
      case other: ApronState =>
        s.getEnvironment.isEqual(other.s.getEnvironment) && s.isEqual(apronManager, other.s)
      case _ => false

    override def hashCode(): Int =
      s.hashCode(apronManager)

    override def toString: String =
      "(env = " + s.getEnvironment.toString + ", state = " + s.toString(apronManager) + ")"

