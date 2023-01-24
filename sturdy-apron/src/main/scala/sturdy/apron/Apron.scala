package sturdy.apron

import apron.*
import sturdy.apron.Apron.{debugAssert, debugJoinWiden}
import sturdy.data.CombineUnit
import sturdy.effect.*
import sturdy.values.*

import java.lang
import java.lang.IllegalStateException
import scala.collection.mutable
import scala.language.reflectiveCalls

object Apron:
  val debugAll: Boolean = true
  val debugAlloc: Boolean = debugAll
  val debugAssign: Boolean = debugAll
  val debugJoinWiden: Boolean = debugAll
  val debugAssert: Boolean = debugAll

  case class Bottom() extends SturdyFailure

class Apron(val apronManager: Manager, val alloc: ApronAlloc) extends Effect:
  val joins: ApronJoins = new ApronJoins(this)

  override def toString: String =
    env.getVars.mkString("Array(", ", ", ") : ") + apronState.toString(apronManager)

  private var apronState: Abstract1 = new Abstract1(apronManager, new Environment())

  private[apron] inline def env: Environment = apronState.getEnvironment

  private var _freedReferences: mutable.WeakHashMap[ApronVar.UID, ApronExpr] = mutable.WeakHashMap()

  inline def getFreedReference(v: ApronVar): Option[ApronExpr] = _freedReferences.get(v.uid)
  private[apron] def freeReference(v: ApronVar, e: ApronExpr): Unit =
    _freedReferences.update(v.uid, e)
  inline def getFreedReferences: Map[ApronVar.UID, ApronExpr] = _freedReferences.toMap
  inline def inScope(v: ApronVar): Boolean = getFreedReference(v).isEmpty

  def getBound(v: ApronVar): Interval = getFreedReference(v) match
    case Some(e) => getBound(e)
    case None => apronState.getBound(apronManager, v.av)

  inline private[apron] def getBound(av: apron.Var): Interval =
    if (env.hasVar(av))
      apronState.getBound(apronManager, av)
    else
      ApronExpr.topInterval

  inline def getBound(v: ApronExpr): Interval =
    apronState.getBound(apronManager, v.toIntern(this))

  def getBound(v: Texpr1Node): Interval =
    val vIntern = new Texpr1Intern(env, v)
    apronState.getBound(apronManager, vIntern)

  def addDoubleVariable(site: ApronAllocationSite): alloc.Var =
    alloc.allocateDoubleVariable(site, this)

  def addIntVariable(site: ApronAllocationSite): alloc.Var =
    alloc.allocateIntVariable(site, this)

  def freeVariable(v: alloc.Var): Unit =
    if (!inScope(v))
      return
    val isStrong = alloc.freeVariable(v, this)
    if (Apron.debugAlloc) {
      println(s"Freeing ${if (isStrong) "strong" else "weak"} $v = ${getBound(v.av)}")
    }
    if (isStrong) {
      val oldVal = getBound(v.av)
      freeReference(v, ApronExpr.Constant(oldVal))
      if (env.hasVar(v.av)) {
        apronState.forget(apronManager, v.av, false)
        val newEnv = apronState.getEnvironment.remove(Array(v.av))
        apronState.changeEnvironment(apronManager, newEnv, false)
      }
    }

  /** Assigns v := exp. In case of a strong assignment, invalidates previous references of v and yields a fresh copy of v to use for future references. */
  def assign(v: alloc.Var, exp: ApronExpr): Option[alloc.Var] =
    if (!inScope(v))
      throw new IllegalStateException(s"Cannot assign to out-of-scope variable $v")

    val isStrong = alloc.useStrongUpdate(v)
    val isInitialized = env.hasVar(v.av)
    var newV: Option[alloc.Var] = None

    if (!isInitialized) {
      initializeVar(v)
      val expIntern = exp.toIntern(this)
      if (Apron.debugAssign) {
        println(s"assigning uninitialized $v = $exp = ${apronState.getBound(apronManager, expIntern)}, was uninitialized")
      }
      apronState.assign(apronManager, v.av, expIntern, null)
    } else if (isStrong) {
      val oldVal = getBound(v.av)
      freeReference(v, ApronExpr.Constant(oldVal))

      val vnew = alloc.freshReference(v)
      val expIntern = exp.toIntern(this)
      if (Apron.debugAssign) {
        println(s"assigning strong $vnew = $exp = ${apronState.getBound(apronManager, expIntern)}, was $oldVal")
      }
      apronState.assign(apronManager, vnew.av, expIntern, null)
      newV = Some(vnew)
    } else {
      val expIntern = exp.toIntern(this)
      val expString = exp.toString
      val old = getBound(v)
      val expBound = apronState.getBound(apronManager, expIntern)
      val assigned = apronState.assignCopy(apronManager, v.av, expIntern, null)
      apronState.join(apronManager, assigned)
      if (Apron.debugAssign) {
        println(s"assigning weak $v = old join $expString = $old join $expBound = ${getBound(v)}, $this")
      }
    }
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")
    newV

  private[apron] def initializeVar(v: ApronVar): ApronVar =
    if (inScope(v) && !env.hasVar(v.av)) {
      val intAr = if (v.isInt) Array(v.av) else null
      val floAr = if (v.isInt) null else Array(v.av)
      apronState.changeEnvironment(apronState.getCreationManager, env.add(intAr, floAr), false)
    }
    v

  def withLocal[A](st: ApronState)(f: => A) =
    val snapshot = apronState
    apronState = st.s
    try f finally {
      apronState = snapshot
    }
  
  def withTemporaryIntVariable[A](f: alloc.Var => A): A =
    val v = addIntVariable(ApronAllocationSite.TemporaryVar)
    val res = try f(v)
    finally {
      freeVariable(v)
    }
    res

  def withTemporaryIntVariables[A](n: Int)(f: PartialFunction[List[alloc.Var], A]): A =
    val vs = (1 to n).toList.map(i => addIntVariable(ApronAllocationSite.TemporaryVar))
    try f(vs)
    finally {
      vs.foreach(freeVariable)
    }

  def withTemporaryDoubleVariable[A](f: alloc.Var => A): A =
    val v = alloc.allocateDoubleVariable(ApronAllocationSite.TemporaryVar, this)
    try f(v)
    finally {
      freeVariable(v)
    }

  def withTemporaryDoubleVariables[A](n: Int)(f: PartialFunction[List[alloc.Var], A]): A =
    val vs = (1 to n).toList.map(i => alloc.allocateDoubleVariable(ApronAllocationSite.TemporaryVar, this))
    try f(vs)
    finally {
      vs.foreach(freeVariable)
    }

  def assertConstrain(ac: ApronCons): Unit =
    val c = ac.toApron(this)
    if (c.size != 1)
      throw new IllegalStateException(s"Cannot assert $ac here, since it translates to multiple constraints $c")
    assertConstrain(c.head)

  private def assertConstrain(c: Tcons1): Unit =
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"Apron state may not be bottom prior to constraining!")
    if (c.getKind == Tcons1.DISEQ)
      throw new IllegalArgumentException("DISEQ constraints should be handled outside of the function!")
    c.extendEnvironment(env)
    val newState = apronState.meetCopy(apronManager, c)
    if (debugAssert)
      println(s"Asserting $c\n  was $apronState\n  now $newState")
    apronState = newState
    if (apronState.isBottom(apronManager))
      throw Apron.Bottom()

  def ifThenElseUnit[A, B](cond: ApronCons)(ifTrue: => A)(ifFalse: => B)(using EffectStack): Unit =
    ifThenElse(cond, cond.negated)({ifTrue; ()})({ifFalse; ()})

  def ifThenElse[A](cond: ApronCons)(ifTrue: => A)(ifFalse: => A)(using EffectStack): Join[A] ?=> A =
    ifThenElse(cond, cond.negated)(ifTrue)(ifFalse)

  def ifThenElse[A](condTrue: ApronCons, condFalse: ApronCons)(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A =
    val cTrue = condTrue.toApron(this)
    val condTrueStr = condTrue.toString
    val cFalse = condFalse.toApron(this)
    val condFalseStr = condFalse.toString

    effects.joinComputations {
      if (debugAssert)
        println(s"if $condTrueStr then")
      effects.joinFold(cTrue, c =>
        assertConstrain(c)
        ifTrue
      )
    } {
      if (debugAssert)
        println(s"else ($condFalseStr)")
      effects.joinFold(cFalse, c =>
        assertConstrain(c)
        ifFalse
      )
    }

  def ifThenElse[A](cond: Topped[ApronCons])(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A = cond match
    case Topped.Top => effects.joinComputations(ifTrue)(ifFalse)
    case Topped.Actual(b) => ifThenElse(b)(ifTrue)(ifFalse)

  def ifThenElsePure[A](condTrue: ApronCons, widen: Boolean)(ifTrue: A)(ifFalse: A): Join[A] ?=> A =
    ifThenElsePure(condTrue, condTrue.negated, widen)(ifTrue)(ifFalse)

  private def ifThenElsePure[A](condTrue: ApronCons, condFalse: ApronCons, widen: Boolean)(ifTrue: A)(ifFalse: A): Join[A] ?=> A =
    val snapshot = apronState

    val cTrue = condTrue.toApron(this)
    val cFalse = condFalse.toApron(this)

    val resTrue = cTrue.map(c => TrySturdy {
      apronState = new Abstract1(apronManager, snapshot)
      assertConstrain(c)
      ifTrue
    }.get)
    val resFalse = cFalse.map(c => TrySturdy {
      apronState = new Abstract1(apronManager, snapshot)
      assertConstrain(c)
      ifFalse
    }.get)
    // restore original state, since no effect can have changed it and because (condTrue || condFalse = true).
    apronState = snapshot
    (resTrue ++ resFalse).flatten.reduce(Join(_, _).get)

  def combineApronStates(s1: Abstract1, s2: Abstract1, widen: Boolean): MaybeChanged[Abstract1] = {
    // TODO review this code on first widening in cfgloop.tip
    if (debugJoinWiden) {
      println(
        s"""${if (widen) "Widening" else "Joining"} apron
           |  s1 = $s1
           |  s2 = $s2""".stripMargin)
    }

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

    if (debugJoinWiden) {
      println(
        s"""  joined = $combined""".stripMargin)
    }
    if (Apron.debugJoinWiden && !combinable1.isIncluded(apronManager, combined))
      throw new IllegalStateException(s"$combinable1 not included in $combined")
    if (Apron.debugJoinWiden && !combinable2.isIncluded(apronManager, combined))
      throw new IllegalStateException(s"$combinable2 not included in $combined")

    val s1Only = combinable1.forgetCopy(apronManager, inboth, false)
    val s2Only = combinable2.forgetCopy(apronManager, inboth, false)
    combined.meet(apronManager, s1Only)
    combined.meet(apronManager, s2Only)
    val changed = !combined.isEqual(apronManager, combinable1)
    if (debugJoinWiden) {
      println(
        s"""  changed = $changed""".stripMargin)
    }
    if (debugJoinWiden && changed && combined.toString(apronManager) == combinable1.toString(apronManager))
      throw new IllegalStateException()
    if (apronState.isBottom(apronManager))
      throw Apron.Bottom()
    MaybeChanged(combined, changed)
  }

  override type State = ApronState
  override def getState: ApronState =
    new ApronState(apronManager, new Abstract1(apronManager, apronState))
  override def setState(as: ApronState): Unit =
    val st = new Abstract1(apronManager, as.s)
    apronState = st
    if (apronState.isBottom(apronManager))
      throw new SturdyFailure {}

  override def join: Join[ApronState] = (as1, as2) => {
    val s1 = as1.s
    val s2 = as2.s
    if (s1.isBottom(apronManager))
      MaybeChanged.Changed(as2)
    else if (s2.isBottom(apronManager))
      MaybeChanged.Unchanged(as1)
    else {
      val joined = combineApronStates(s1, s2, widen = false)
      val result = joined.map(new ApronState(apronManager, _))
      result
    }
  }

  override def widen: Widen[ApronState] = (as1, as2) => {
    val s1 = as1.s
    val s2 = as2.s
    if (s1.isBottom(apronManager))
      MaybeChanged.Changed(as2)
    else if (s2.isBottom(apronManager))
      MaybeChanged.Unchanged(as1)
    else {
      val widened = combineApronStates(s1, s2, widen = true)
      val result = widened.map(new ApronState(apronManager, _))
      result
    }
  }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ApronComputationJoiner[A])

  class ApronComputationJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = new Abstract1(apronManager, apronState)
    private val snapshotFreedReferences = getFreedReferences
    private var fState: Abstract1 = _
    private var fFreedReferences: Map[ApronVar.UID, ApronExpr] = _

    override def inbetween(): Unit =
      fState = apronState
      fFreedReferences = getFreedReferences
      apronState = new Abstract1(apronManager, snapshot)
      _freedReferences = mutable.WeakHashMap.from(snapshotFreedReferences)
//      extendEnvironment(fState.getEnvironment)

    override def retainNone(): Unit =
      apronState = snapshot
      _freedReferences = mutable.WeakHashMap.from(snapshotFreedReferences)

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      apronState = fState
      _freedReferences = mutable.WeakHashMap.from(fFreedReferences)

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      val gState = apronState
      apronState = combineApronStates(fState, gState, widen = false).get
      val st = getState
      for ((v, e1) <- fFreedReferences)
        _freedReferences.updateWith(v) {
          case None => Some(e1)
          case Some(e2) => Some(joins.combineExprs(e1, e2, st, widen = false).get)
        }
  }

class ApronState(apronManager: apron.Manager, val s: Abstract1):
  override def equals(obj: Any): Boolean = obj match
    case other: ApronState =>
      s.getEnvironment.isEqual(other.s.getEnvironment) && s.isEqual(apronManager, other.s)
    case _ =>
      false

  override def hashCode(): Int =
    s.hashCode(apronManager)

  override def toString: String =
    "(env = " + s.getEnvironment.toString + ", state = " + s.toString(apronManager) + ")"



