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

class Apron(val apronManager: Manager, val alloc: ApronAlloc) extends Effect:
  override def toString: String =
    apronEnv.getVars.mkString("Array(", ", ", ") : ") + apronState.toString(apronManager)

  private var apronState: Abstract1 = new Abstract1(apronManager, new Environment())

  def apronEnv: Environment = apronState.getEnvironment

//  def setLeastExtendingEnvironment(other: Abstract1): Unit = {
//    val lce = apronEnv.lce(other.getEnvironment)
//    apronState.changeEnvironment(apronManager, lce, false)
//    other.changeEnvironment(apronManager, lce, false)
//  }

//  def extendEnvironment(superEnv: Environment): Unit = {
//    apronState.changeEnvironment(apronManager, superEnv, false)
//  }

  def getBound(v: alloc.Var): Interval =
    v.getBound(apronState)

  def getBound(v: ApronExpr): Interval =
    getBound(v.toApron)

  def getBound(v: Texpr1Node): Interval =
    val vIntern = try {
      new Texpr1Intern(apronEnv, v)
    } catch {
      case e: IllegalArgumentException if e.getMessage == "no such variable" =>
        return null
    }
    apronState.getBound(apronManager, vIntern)

  def addDoubleVariable(site: ApronAllocationSite): alloc.Var =
    alloc.addDoubleVariable(apronState, site)

  def addIntVariable(site: ApronAllocationSite): alloc.Var =
    alloc.addIntVariable(apronState, site)

  def freeVariable(v: alloc.Var): Unit =
    alloc.freeVariable(v, apronState)

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
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"Apron state may not be bottom prior to constraining!")
    ac.vars.foreach(_.ensureInitialized(apronState))
    val c = ac.toApron(apronEnv)
    if(c.getKind == Tcons1.DISEQ)
      throw new IllegalArgumentException("DISEQ constraints should be handled outside of the function!")
    c.extendEnvironment(apronEnv)
    apronState.meet(apronManager, c)
    if (debugAssert)
      println(s"Asserting $ac yielding $this")
    if (apronState.isBottom(apronManager))
      throw new SturdyFailure {}

  /** Assigns v := exp. In case of a strong assignment, invalidates previous references of v and yields a fresh copy of v to use for future references. */
  def assign(v: alloc.Var, exp: ApronExpr): Option[alloc.Var] =
    val av = v.getOrElse(throw new IllegalStateException(s"Cannot assign to freed variable $v"))
    val isStrong = alloc.useStrongUpdate(v)
    val isInitialized = v.isInitialized(apronState)
    var newV: Option[alloc.Var] = None

    if (!isInitialized) {
      v.initialize(apronState)
      val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
      if (Apron.debugAssign)
        println(s"assigning uninitialized $v = $exp = ${apronState.getBound(apronManager, expIntern)}, was uninitialized")
      apronState.assign(apronManager, v.av, expIntern, null)
    } else if (isStrong) {
      v.free(apronState)
      if (Apron.debugAlloc)
        println(s"freeing old references of $v (ref count ${v.refCount}) = ${v.getBound(apronState)}")

      val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
      if (Apron.debugAssign) {
        val old = getBound(v)
        println(s"assigning strong $v = $exp = ${apronState.getBound(apronManager, expIntern)}, was $old")
      }
      apronState.assign(apronManager, av, expIntern, null)
      newV = Some(alloc.freshReference(v))
    } else {
      val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
      if (Apron.debugAssign) {
        val old = getBound(v)
        print(s"assigning weak $v = old join $exp = $old join ${apronState.getBound(apronManager, expIntern)} = ")
      }
      val assigned = apronState.assignCopy(apronManager, av, expIntern, null)
      apronState.join(apronManager, assigned)
      if (Apron.debugAssign)
        println(getBound(v))
    }
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")
    newV

  def withTemporaryIntVariable[A](f: alloc.Var => A): A =
    val v = alloc.addIntVariable(apronState, ApronAllocationSite.TemporaryVar)
    try f(v)
    finally {
      alloc.freeVariable(v, apronState)
    }

  def withTemporaryIntVariables[A](n: Int)(f: PartialFunction[List[alloc.Var], A]): A =
    val vs = (1 to n).toList.map(i => alloc.addIntVariable(apronState, ApronAllocationSite.TemporaryVar))
    try f(vs)
    finally {
      vs.foreach(alloc.freeVariable(_, apronState))
    }

  def withTemporaryDoubleVariable[A](f: alloc.Var => A): A =
    val v = alloc.addDoubleVariable(apronState, ApronAllocationSite.TemporaryVar)
    try f(v)
    finally {
      alloc.freeVariable(v, apronState)
    }

  def withTemporaryDoubleVariables[A](n: Int)(f: PartialFunction[List[alloc.Var], A]): A =
    val vs = (1 to n).toList.map(i => alloc.addDoubleVariable(apronState, ApronAllocationSite.TemporaryVar))
    try f(vs)
    finally {
      vs.foreach(alloc.freeVariable(_, apronState))
    }

  def ifThenElseUnit[A, B](cond: ApronCons)(ifTrue: => A)(ifFalse: => B)(using EffectStack): Unit =
    ifThenElse(cond, cond.negated)({ifTrue; ()})({ifFalse; ()})

  def ifThenElse[A](cond: ApronCons)(ifTrue: => A)(ifFalse: => A)(using EffectStack): Join[A] ?=> A =
    ifThenElse(cond, cond.negated)(ifTrue)(ifFalse)

  def ifThenElse[A](condTrue: ApronCons, condFalse: ApronCons)(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A =
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
    val snapshot = new Abstract1(apronManager, apronState)
    val res1 = TrySturdy(constrainPure(condTrue)(ifTrue))
    val state1 = apronState
    apronState = new Abstract1(apronManager, snapshot)
//    extendEnvironment(state1.getEnvironment)
    val res2 = TrySturdy(constrainPure(condFalse)(ifFalse))

    (res1.isBottom, res2.isBottom) match
      case (false, false) =>
        val state2 = apronState
        apronState = combineApronStates(state1, state2, widen).get
      case (false, true) =>
        apronState = state1
      case (true, false) =>
        // nothing
      case (true, true) =>
        apronState = snapshot

    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")
    Join(res1, res2).get.getOrThrow

  def combineApronStates(s1: Abstract1, s2: Abstract1, widen: Boolean): MaybeChanged[Abstract1] =
    val f: (Abstract1, Abstract1) => Abstract1 =
      if (widen)
        _.widening(apronManager, _)
      else
        _.joinCopy(apronManager, _)
    combineApronStates(s1, s2, f)

  def combineApronStates(s1: Abstract1, s2: Abstract1, combine: (Abstract1, Abstract1) => Abstract1): MaybeChanged[Abstract1] = {
    val vars1 = s1.getEnvironment.getVars.toSet
    val vars2 = s2.getEnvironment.getVars.toSet
    val inboth = (vars1 intersect vars2).toArray

    val lce = s1.getEnvironment.lce(s2.getEnvironment)
    val combinable1 = s1.changeEnvironmentCopy(apronManager, lce, false)
    val combinable2 = s2.changeEnvironmentCopy(apronManager, lce, false)
    val combined = combine(combinable1, combinable2)

    val s1Only = combinable1.forgetCopy(apronManager, inboth, false)
    val s2Only = combinable2.forgetCopy(apronManager, inboth, false)
    combined.meet(apronManager, s1Only)
    combined.meet(apronManager, s2Only)
    val changed = !combined.isEqual(apronManager, combinable1)
    if (debugJoinWiden && changed && combined.toString(apronManager) == combinable1.toString(apronManager))
      throw new IllegalStateException()
    if (apronState.isBottom(apronManager))
      throw new SturdyFailure {}
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

  //    val x = addIntVariable(if (widen) "widen" else "join", ApronAllocationSite.Join(v1, v2, widen))
//    val v1Cons = makeConstraint(new Texpr1BinNode(Texpr1BinNode.OP_SUB, x.node, v1), Tcons1.EQ)
//    val v2Cons = makeConstraint(new Texpr1BinNode(Texpr1BinNode.OP_SUB, x.node, v2), Tcons1.EQ)
//    ifThenElsePure(v1Cons, v2Cons, widen)(())(())
//
//    val xBound = getBound(x)
//    val v1Bound = getBound(v1)
//    MaybeChanged(x, !xBound.isEqual(v1Bound))

//  def joinValuesVarNode(v1: Texpr1Node, v2: Texpr1Node, widen: Boolean): MaybeChanged[Texpr1VarNode] =
//    joinValues(v1, v2, widen).map(av => av.node)

  override type State = ApronState

  override def getState: ApronState =
    new ApronState(new Abstract1(apronManager, apronState))
  override def setState(as: ApronState): Unit =
    val st = new Abstract1(apronManager, as.s)
//    setLeastExtendingEnvironment(st)
    val currentEnv = apronEnv
    apronState = st
//    extendEnvironment(currentEnv)
    if (apronState.isBottom(apronManager))
      throw new SturdyFailure {}

  override def join: Join[State] = (as1, as2) => {
    val s1 = as1.s
    val s2 = as2.s
    if (s1.isBottom(apronManager))
      MaybeChanged.Changed(as2)
    else if (s2.isBottom(apronManager))
      MaybeChanged.Unchanged(as1)
    else {
      val joined = combineApronStates(s1, s2, _.joinCopy(apronManager, _))
      val result = joined.map(new ApronState(_))
      if (debugJoinWiden) {
        println(
          s"""Joining apron
             |  s1 = $as1
             |  s2 = $as2
             |  joined = $result""".stripMargin)
      }
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
      val widened = combineApronStates(s1, s2, _.widening(apronManager, _))
      val result = widened.map(new ApronState(_))
      if (debugJoinWiden) {
        println(
          s"""Widening apron
             |  s1 = $as1
             |  s2 = $as2
             |  widened = $result""".stripMargin)
      }
      result
    }
  }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A] {
    private val snapshot = new Abstract1(apronManager, apronState)
    private var fState: Abstract1 = _

    override def inbetween(): Unit =
      fState = apronState
      apronState = new Abstract1(apronManager, snapshot)
//      extendEnvironment(fState.getEnvironment)

    override def retainNone(): Unit =
      apronState = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      apronState = fState

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      val gState = apronState
      apronState = combineApronStates(fState, gState, _.joinCopy(apronManager, _)).get
  })

  class ApronState(val s: Abstract1):
    override def equals(obj: Any): Boolean = obj match
      case other: ApronState =>
        s.getEnvironment.isEqual(other.s.getEnvironment) && s.isEqual(apronManager, other.s)
      case _ => false

    override def hashCode(): Int =
      s.hashCode(apronManager)

    override def toString: String =
      "(env = " + s.getEnvironment.toString + ", state = " + s.toString(apronManager) + ")"

