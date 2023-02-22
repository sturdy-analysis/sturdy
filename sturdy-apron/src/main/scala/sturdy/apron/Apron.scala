package sturdy.apron

import apron.*
import sturdy.apron.Apron.{debugAssert, debugJoinWiden}
import sturdy.data.{CombineUnit, JoinMap, WidenFiniteKeyMap, combineMaps}
import sturdy.effect.*
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.values.*

import java.lang
import java.lang.IllegalStateException
import scala.collection.mutable
import scala.language.reflectiveCalls

object Apron:
  val debugAll: Boolean = false
  val debugAlloc: Boolean = debugAll
  val debugAssign: Boolean = debugAll
  val debugJoinWiden: Boolean = debugAll
  val debugAssert: Boolean = debugAll
  val debugScope: Boolean = debugAll

  case object Bottom extends FailureKind

/**
  * Provides apron as an effectful analysis component.
  *
  * State = (Manager, apron.Abstract1, FreedRefs)
  */
class Apron(val apronManager: Manager, val alloc: ApronAlloc)(using Failure) extends Effect:
  val joins: ApronJoins = ApronJoins(apronManager)

  override def toString: String =
    s"env = [${env.getVars.mkString(",")}], state = ${apronState.toString(apronManager)}, free = $_freedReferences"

  private var apronState: Abstract1 = new Abstract1(apronManager, new Environment())

  private[apron] inline def env: Environment = apronState.getEnvironment

  private var _freedReferences: Map[ApronVar.UID, ApronExpr] = Map()

  private def freeReference(v: ApronVar, e: ApronExpr): Unit =
    if (Apron.debugAlloc)
      println(s"Free reference $v = $e")
    _freedReferences += v.uid -> e
  private inline def getFreedReferences: Map[ApronVar.UID, ApronExpr] = _freedReferences

  val currentScope: ApronScope = new ApronScope:
    override def toString: String = "current scope " + Apron.this.toString
    override def apronEnv: Environment = apronState.getEnvironment
    override def getBound(v: ApronVar): Interval = getFreedReference(v) match
      case Some(e) => getBound(e)
      case None =>
        if (env.hasVar(v.av))
          apronState.getBound(apronManager, v.av)
        else
          ApronExpr.topInterval
    override def getBound(e: ApronExpr): Interval =
      apronState.getBound(apronManager, e.toIntern(this, allowOpen = false))
    override def getFreedReference(v: ApronVar): Option[ApronExpr] =
      _freedReferences.get(v.uid)
  import currentScope.*

  def addDoubleVariable(site: ApronAllocationSite): alloc.Var =
    alloc.allocateDoubleVariable(site, this)

  def addIntVariable(site: ApronAllocationSite): alloc.Var =
    alloc.allocateIntVariable(site, this)

  def freeVariable(v: alloc.Var): Unit =
    if (isFreed(v))
      return
    val isStrong = alloc.freeVariable(v, this)
    if (Apron.debugAlloc) {
      println(s"Freeing ${if (isStrong) "strong" else "weak"} $v = ${getBound(v)}")
    }
    if (isStrong) {
      val oldVal = getBound(v)
      freeReference(v, ApronExpr.Constant(oldVal))
      if (env.hasVar(v.av)) {
        apronState.forget(apronManager, v.av, false)
        val newEnv = apronState.getEnvironment.remove(Array(v.av))
        apronState.changeEnvironment(apronManager, newEnv, false)
      }
    }

  /** Assigns v := exp. In case of a strong assignment, invalidates previous references of v and yields a fresh copy of v to use for future references. */
  def assign(v: alloc.Var, exp: ApronExpr): Option[alloc.Var] =
    if (isFreed(v))
      throw new IllegalStateException(s"Cannot assign to out-of-scope variable $v")

    val isStrong = alloc.useStrongUpdate(v)
    val isInitialized = env.hasVar(v.av)
    var newV: Option[alloc.Var] = None

    if (!isInitialized) {
      initializeVar(v)
      val expIntern = exp.toIntern(currentScope)
      if (Apron.debugAssign) {
        println(s"assigning uninitialized $v = $exp = ${apronState.getBound(apronManager, expIntern)}, was uninitialized")
      }
      apronState.assign(apronManager, v.av, expIntern, null)
    } else if (isStrong) {
      val oldVal = getBound(v)

      val vnew = alloc.freshReference(v)
      val expIntern = exp.toIntern(currentScope)
      if (Apron.debugAssign) {
        println(s"assigning strong $vnew = $exp = ${apronState.getBound(apronManager, expIntern)}, was $oldVal")
      }
      apronState.assign(apronManager, vnew.av, expIntern, null)
      freeReference(v, ApronExpr.Constant(oldVal))

      newV = Some(vnew)
    } else {
      val expIntern = exp.toIntern(currentScope)
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
    if (!isFreed(v) && !env.hasVar(v.av)) {
      val intAr = if (v.isInt) Array(v.av) else null
      val floAr = if (v.isInt) null else Array(v.av)
      apronState.changeEnvironment(apronManager, env.add(intAr, floAr), false)
    }
    v

  def withLocal[A](st: ApronState)(f: => A) =
    val snapshot = apronState
    apronState = st.cs
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
    val c = ac.toApron(currentScope)
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
      Failure(Apron.Bottom, s"Asserting $c\n  was $apronState\n  now $newState")

  def ifThenElseUnit[A, B](cond: ApronCons)(ifTrue: => A)(ifFalse: => B)(using EffectStack): Unit =
    ifThenElse(cond, cond.negated)({ifTrue; ()})({ifFalse; ()})

  def ifThenElse[A](cond: ApronCons)(ifTrue: => A)(ifFalse: => A)(using EffectStack): Join[A] ?=> A =
    ifThenElse(cond, cond.negated)(ifTrue)(ifFalse)

  def ifThenElse[A](condTrue: ApronCons, condFalse: ApronCons)(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A =
    val cTrue = condTrue.toApron(currentScope)
    val condTrueStr = condTrue.toString
    val cFalse = condFalse.toApron(currentScope)
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

    val cTrue = condTrue.toApron(currentScope)
    val cFalse = condFalse.toApron(currentScope)

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


  def locally[A](f: => A): A =
    val snapState = new Abstract1(apronManager, apronState)
    try f finally
      apronState = snapState

  def setInternalState(st: Abstract1): Unit =
    apronState = st

  override type State = ApronState
  override def getState: ApronState =
    new ApronState(apronManager, new Abstract1(apronManager, apronState), getFreedReferences)
  override def setState(as: ApronState): Unit =
    val st = new Abstract1(apronManager, as.cs)
    apronState = st
    _freedReferences = as.freed
    if (apronState.isBottom(apronManager))
      Failure(Apron.Bottom, s"Cannot set bottom state")

  override def join: Join[ApronState] = (as1, as2) => {
    val s1 = as1.cs
    val s2 = as2.cs
    if (s1.isBottom(apronManager))
      MaybeChanged(as2, !s2.isBottom(apronManager))
    else if (s2.isBottom(apronManager))
      MaybeChanged.Unchanged(as1)
    else {
      val joined = joins.combineApronStates(s1, s2, widen = false)
      val freedJoined = JoinMap(using JoinApronExpr(using this))(as1.freed, as2.freed)
      if (debugJoinWiden) {
        println(
          s"""Joining apron freed
             |  freed1 = ${as1.freed}
             |  freed2 = ${as2.freed}
             |  joined = ${freedJoined.get}
             |  changed = ${freedJoined.hasChanged}""".stripMargin)
      }
      val result = MaybeChanged(new ApronState(apronManager, joined.get, freedJoined.get), joined.hasChanged)
      result
    }
  }

  override def widen: Widen[ApronState] = (as1, as2) => {
    val s1 = as1.cs
    val s2 = as2.cs
    if (s1.isBottom(apronManager))
      MaybeChanged(as2, !s2.isBottom(apronManager))
    else if (s2.isBottom(apronManager))
      MaybeChanged.Unchanged(as1)
    else {
      val widened = joins.combineApronStates(s1, s2, widen = true)
      val freedWidened = WidenFiniteKeyMap(using WidenApronExpr(using this), new Finite[ApronVar.UID] {})(as1.freed, as2.freed)
      if (debugJoinWiden) {
        println(
          s"""Widening apron freed
             |  freed1 = ${as1.freed.toList}
             |  freed2 = ${as2.freed.toList}
             |  joined = ${freedWidened.get.toList}
             |  changed = ${freedWidened.hasChanged}""".stripMargin)
      }
      val result = MaybeChanged(new ApronState(apronManager, widened.get, freedWidened.get), widened.hasChanged)
      result
    }
  }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ApronComputationJoiner[A])

  class ApronComputationJoiner[A] extends ComputationJoiner[A] {
    private val snapshotFreedReferences = getFreedReferences
    private val snapshot = new Abstract1(apronManager, apronState)
    private var fState: Abstract1 = _
    private var fFreedReferences: Map[ApronVar.UID, ApronExpr] = _

    override def inbetween(): Unit =
      fState = apronState
      fFreedReferences = getFreedReferences
      apronState = new Abstract1(apronManager, snapshot)
      _freedReferences = snapshotFreedReferences
//      extendEnvironment(fState.getEnvironment)

    override def retainNone(): Unit =
      apronState = snapshot
      _freedReferences = snapshotFreedReferences

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      apronState = fState
      _freedReferences = fFreedReferences

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      val gState = apronState
      val combined = joins.combineApronStates(fState, gState, widen = false).get
      val fFree = fFreedReferences
      val gFree = _freedReferences
      apronState = combined
      val st = getState
      _freedReferences = combineMaps(fFree, gFree, joins.combineExprs(_, _, st, widen = false).get)
      if (Apron.debugJoinWiden)
        println(
          s"""Computation joiner apron
             |  fState = $fState
             |  gState = $gState
             |  combined = $combined
             |  fFree = ${fFree.toList}
             |  gFree = ${gFree.toList}
             |  free = ${_freedReferences.toList}""".stripMargin)
  }

class ApronState(apronManager: apron.Manager, val cs: Abstract1, val freed: Map[ApronVar.UID, ApronExpr]) extends ApronScope:
  override def equals(obj: Any): Boolean = obj match
    case other: ApronState =>
      inline def sameEnv = cs.getEnvironment.isEqual(other.cs.getEnvironment)
      inline def sameState = cs.isEqual(apronManager, other.cs)
      inline def sameFreed = true // freed == other.freed
      sameEnv && sameState && sameFreed
    case _ =>
      false

  override def hashCode(): Int =
    cs.hashCode(apronManager) //  + freed.hashCode()

  override def toString: String =
    s"(env = ${cs.getEnvironment}, state = ${cs.toString(apronManager)}, freed = $freed)"

  override def apronEnv: Environment = cs.getEnvironment

  override def getBound(v: ApronVar): Interval = getFreedReference(v) match
    case Some(e) => getBound(e)
    case None =>
      if (cs.getEnvironment.hasVar(v.av))
        cs.getBound(apronManager, v.av)
      else
        ApronExpr.topInterval

  override def getBound(v: ApronExpr): Interval =
    cs.getBound(apronManager, v.toIntern(this, allowOpen = false))

  override def getFreedReference(v: ApronVar): Option[ApronExpr] =
    freed.get(v.uid)

  def copy(newCs: Abstract1) =
    new ApronState(apronManager, newCs, freed)

  def copy(newFreed: Map[ApronVar.UID, ApronExpr]) =
    new ApronState(apronManager, cs, newFreed)

  def copy(newCs: Abstract1, newFreed: Map[ApronVar.UID, ApronExpr]) =
    new ApronState(apronManager, newCs, newFreed)
