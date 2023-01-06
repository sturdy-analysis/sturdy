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
  val debugAll: Boolean = true
  val debugAlloc: Boolean = debugAll
  val debugAssign: Boolean = debugAll
  val debugJoinWiden: Boolean = debugAll
  val debugAssert: Boolean = debugAll

  case class Bottom() extends SturdyFailure

class Apron(val apronManager: Manager, val alloc: ApronAlloc) extends Effect:
  override def toString: String =
    apronEnv.getVars.mkString("Array(", ", ", ") : ") + _apronState.toString(apronManager)

  private var _apronState: Abstract1 = new Abstract1(apronManager, new Environment())
  def apronState: Abstract1 = _apronState

  def apronEnv: Environment = _apronState.getEnvironment

  def getBound(v: alloc.Var): Interval =
    v.getBound(this)

  def getAVBound(av: apron.Var): Interval =
    if (apronEnv.hasVar(av))
      _apronState.getBound(apronManager, av)
    else 
      ApronExpr.top.coeff.asInstanceOf[Interval]

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
    alloc.allocateDoubleVariable(site)

  def addIntVariable(site: ApronAllocationSite): alloc.Var =
    alloc.allocateIntVariable(site)

  def freeVariable(v: alloc.Var): Unit =
    val freeAV = alloc.freeVariable(v, this)
    if (freeAV && apronEnv.hasVar(v.av)) {
      _apronState.forget(apronManager, v.av, false)
      val newEnv = _apronState.getEnvironment.remove(Array(v.av))
      _apronState.changeEnvironment(apronManager, newEnv, false)
    }


  /** Assigns v := exp. In case of a strong assignment, invalidates previous references of v and yields a fresh copy of v to use for future references. */
  def assign(v: alloc.Var, exp: ApronExpr): Option[alloc.Var] =
    if (v.isDelegated)
      throw new IllegalStateException(s"Cannot assign to freed variable $v")

    val isStrong = alloc.useStrongUpdate(v)
    val isInitialized = apronState.getEnvironment.hasVar(v.av)
    var newV: Option[alloc.Var] = None

    exp.vars.foreach(initializeVar)

    if (!isInitialized) {
      initializeVar(v)
      val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
      if (Apron.debugAssign) {
        println(s"assigning uninitialized $v = $exp = ${_apronState.getBound(apronManager, expIntern)}, was uninitialized")
      }
      _apronState.assign(apronManager, v.av, expIntern, null)
    } else if (isStrong) {
      val oldVal = v.getBound(this)
      v.setDelegate(ApronExpr.Constant(oldVal))

      val vnew = alloc.freshReference(v)
      val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
      if (Apron.debugAssign) {
        println(s"assigning strong $vnew = $exp = ${_apronState.getBound(apronManager, expIntern)}, was $oldVal")
      }
      _apronState.assign(apronManager, vnew.av, expIntern, null)
      newV = Some(vnew)
    } else {
      val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
      println(this)
      val old = getBound(v)
      val expBound = _apronState.getBound(apronManager, expIntern)
      val assigned = _apronState.assignCopy(apronManager, v.av, expIntern, null)
      _apronState.join(apronManager, assigned)
      if (Apron.debugAssign) {
        println(s"assigning weak $v = old join $exp = $old join $expBound = ${getBound(v)}")
      }
    }
    if (_apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")
    newV

  private def initializeVar(v: ApronVar): Unit =
    if (v.isOpen && !apronEnv.hasVar(v.av)) {
      val intAr = if (v.isInt) Array(v.av) else null
      val floAr = if (v.isInt) null else Array(v.av)
      apronState.changeEnvironment(apronState.getCreationManager, apronState.getEnvironment.add(intAr, floAr), false)
    }

  def withTemporaryIntVariable[A](f: alloc.Var => A): A =
    val v = alloc.allocateIntVariable(ApronAllocationSite.TemporaryVar)
    val res = try f(v)
    finally {
      freeVariable(v)
    }
    res

  def withTemporaryIntVariables[A](n: Int)(f: PartialFunction[List[alloc.Var], A]): A =
    val vs = (1 to n).toList.map(i => alloc.allocateIntVariable(ApronAllocationSite.TemporaryVar))
    try f(vs)
    finally {
      vs.foreach(freeVariable)
    }

  def withTemporaryDoubleVariable[A](f: alloc.Var => A): A =
    val v = alloc.allocateDoubleVariable(ApronAllocationSite.TemporaryVar)
    try f(v)
    finally {
      freeVariable(v)
    }

  def withTemporaryDoubleVariables[A](n: Int)(f: PartialFunction[List[alloc.Var], A]): A =
    val vs = (1 to n).toList.map(i => alloc.allocateDoubleVariable(ApronAllocationSite.TemporaryVar))
    try f(vs)
    finally {
      vs.foreach(freeVariable)
    }

  def assertConstrain(ac: ApronCons): Unit =
    ac.vars.foreach(initializeVar)
    val c = ac.toApron(apronEnv)
    if (c.size != 1)
      throw new IllegalStateException(s"Cannot assert $ac here, since it translates to multiple constraints $c")
    assertConstrain(c.head)

  private def assertConstrain(c: Tcons1): Unit =
    if (_apronState.isBottom(apronManager))
      throw new IllegalStateException(s"Apron state may not be bottom prior to constraining!")
    if (c.getKind == Tcons1.DISEQ)
      throw new IllegalArgumentException("DISEQ constraints should be handled outside of the function!")
    c.extendEnvironment(apronEnv)
    val newState = _apronState.meetCopy(apronManager, c)
    if (debugAssert)
      println(s"Asserting $c\n  was $_apronState\n  now $newState")
    _apronState = newState
    if (_apronState.isBottom(apronManager))
      throw Apron.Bottom()

  def ifThenElseUnit[A, B](cond: ApronCons)(ifTrue: => A)(ifFalse: => B)(using EffectStack): Unit =
    ifThenElse(cond, cond.negated)({ifTrue; ()})({ifFalse; ()})

  def ifThenElse[A](cond: ApronCons)(ifTrue: => A)(ifFalse: => A)(using EffectStack): Join[A] ?=> A =
    ifThenElse(cond, cond.negated)(ifTrue)(ifFalse)

  def ifThenElse[A](condTrue: ApronCons, condFalse: ApronCons)(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A =
    condTrue.vars.foreach(initializeVar)
    condFalse.vars.foreach(initializeVar)

    val cTrue = condTrue.toApron(apronEnv)
    val condTrueStr = condTrue.toString
    val cFalse = condFalse.toApron(apronEnv)
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
    condTrue.vars.foreach(initializeVar)
    condFalse.vars.foreach(initializeVar)

    val snapshot = _apronState

    val cTrue = condTrue.toApron(apronEnv)
    val cFalse = condFalse.toApron(apronEnv)

    val resTrue = cTrue.map(c => TrySturdy {
      _apronState = new Abstract1(apronManager, snapshot)
      assertConstrain(c)
      ifTrue
    }.get)
    val resFalse = cFalse.map(c => TrySturdy {
      _apronState = new Abstract1(apronManager, snapshot)
      assertConstrain(c)
      ifFalse
    }.get)
    // restore original state, since no effect can have changed it and because (condTrue || condFalse = true).
    _apronState = snapshot
    (resTrue ++ resFalse).flatten.reduce(Join(_, _).get)

  def combineApronStates(s1: Abstract1, s2: Abstract1, widen: Boolean): MaybeChanged[Abstract1] = {
    // TODO review this code on first widening in cfgloop.tip
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
           |  joined = $combined
           |  changed = $changed""".stripMargin)
    }
    if (debugJoinWiden && changed && combined.toString(apronManager) == combinable1.toString(apronManager))
      throw new IllegalStateException()
    if (_apronState.isBottom(apronManager))
      throw Apron.Bottom()
    MaybeChanged(combined, changed)
  }

  def joinValues(e1: ApronExpr, e2: ApronExpr, widen: Boolean): MaybeChanged[ApronExpr] =
    if (e1 == e2) {
      MaybeChanged.Unchanged(e1)
    } else {
      withTemporaryIntVariable { x =>
        val oldBound = getBound(e1)

        initializeVar(x)
        val e1Intern = new Texpr1Intern(apronEnv, e1.toApron)
        val a1 = _apronState.assignCopy(apronManager, x.av, e1Intern, null)
        val e2Intern = new Texpr1Intern(apronEnv, e2.toApron)
        val a2 = _apronState.assignCopy(apronManager, x.av, e2Intern, null)
        a1.join(apronManager, a2)

        val xBound = a1.getBound(apronManager, x.av)
        if (Apron.debugJoinWiden)
          println(s"Join values $e1 and $e2 is ${x.expr}, widen = $widen")
        MaybeChanged(x.expr, !xBound.isEqual(oldBound))
      }
    }

  override type State = ApronState

  override def getState: ApronState =
    new ApronState(new Abstract1(apronManager, _apronState))
  override def setState(as: ApronState): Unit =
    val st = new Abstract1(apronManager, as.s)
    _apronState = st
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
      case _ =>
        false

    override def hashCode(): Int =
      s.hashCode(apronManager)

    override def toString: String =
      "(env = " + s.getEnvironment.toString + ", state = " + s.toString(apronManager) + ")"

