package sturdy.apron

import apron.{Texpr0UnNode, Environment, Texpr1CstNode, Texpr1UnNode, Interval, MpqScalar, Texpr1VarNode, Linexpr1, Texpr1Node, Tcons0, Tcons1, StringVar, Texpr1BinNode, Texpr1Intern, Manager, Abstract1}
import sturdy.apron.Apron.debugWiden
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
  val debugWiden: Boolean = true


class Apron(val apronManager: Manager, val alloc: ApronAlloc) extends Effect:
  override def toString: String =
    apronEnv.getVars.mkString("Array(", ", ", ") : ") + apronState.toString(apronManager)

  private var apronState: Abstract1 = new Abstract1(apronManager, new Environment())

  def apronEnv: Environment = apronState.getEnvironment

  def setLeastExtendingEnvironment(other: Abstract1): Unit = {
    val lce = apronEnv.lce(other.getEnvironment)
    apronState.changeEnvironment(apronManager, lce, false)
    other.changeEnvironment(apronManager, lce, false)
  }


  def getBound(v: alloc.Var): Interval =
    val intern = new Texpr1Intern(apronState.getEnvironment, v.node)
    apronState.getBound(apronManager, intern)

  def getBound(v: ApronExpr): Interval =
    getBound(v.toApron)

  def getBound(v: Texpr1Node): Interval =
    val vIntern = new Texpr1Intern(apronEnv, v)
    apronState.getBound(apronManager, vIntern)

  def addDoubleVariable(name: String, site: ApronAllocationSite): alloc.Var =
    alloc.addDoubleVariable(name, apronState, site)

  def addIntVariable(name: String, site: ApronAllocationSite): alloc.Var =
    alloc.addIntVariable(name, apronState, site)

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
    val c = ac.toApron(apronEnv)
    if(c.getKind == Tcons1.DISEQ)
      throw new IllegalArgumentException("DISEQ constraints should be handled outside of the function!")
    c.extendEnvironment(apronEnv)
    apronState.meet(apronManager, c)
    if (apronState.isBottom(apronManager))
      throw new SturdyFailure {}

  def assign(v: alloc.Var, exp: ApronExpr): Unit =
    val av = v.getOrElse(throw new IllegalStateException(s"Cannot assign to freed variable $v"))
    val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
    if (alloc.useStrongUpdate(v)) {
      if (Apron.debugAssign) {
        val old = apronState.getBound(apronManager, av)
        println(s"assigning strong $v = $exp = ${apronState.getBound(apronManager, expIntern)}, was $old")
      }
      apronState.assign(apronManager, av, expIntern, null)
    } else {
      if (Apron.debugAssign) {
        val old = apronState.getBound(apronManager, av)
        print(s"assigning weak $v = old join $exp = $old join ${apronState.getBound(apronManager, expIntern)} = ")
      }
      val assigned = apronState.assignCopy(apronManager, av, expIntern, null)
      apronState.join(apronManager, assigned)
      if (Apron.debugAssign)
        println(apronState.getBound(apronManager, av))
    }
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")

  def assignMonotone(v: alloc.Var, exp: ApronExpr): Unit =
    val av = v.getOrElse(throw new IllegalStateException(s"Cannot assign to freed variable $v"))
    val expIntern = new Texpr1Intern(apronEnv, exp.toApron)
    if (Apron.debugAssign) {
      val old = apronState.getBound(apronManager, av)
      println(s"assigning monotone $v = $exp = ${apronState.getBound(apronManager, expIntern)}, was $old")
    }
    apronState.assign(apronManager, av, expIntern, null)
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")

  def freshConstraintVariable(purpose: String, site: ApronAllocationSite): alloc.Var =
    addIntVariable(purpose, site)

  def withTemporaryIntVariable[A](f: alloc.Var => A): A =
    val v = alloc.addIntVariable("$$temporary", apronState, ApronAllocationSite.TemporaryVar)
    try f(v)
    finally {
      alloc.freeVariable(v, apronState)
    }

  def withTemporaryIntVariables[A](n: Int)(f: PartialFunction[List[alloc.Var], A]): A =
    val vs = (1 to n).toList.map(i => alloc.addIntVariable(s"$$temporary_$i", apronState, ApronAllocationSite.TemporaryVar))
    try f(vs)
    finally {
      vs.foreach(alloc.freeVariable(_, apronState))
    }

  def withTemporaryDoubleVariable[A](f: alloc.Var => A): A =
    val v = alloc.addDoubleVariable("$$temporary", apronState, ApronAllocationSite.TemporaryVar)
    try f(v)
    finally {
      alloc.freeVariable(v, apronState)
    }

  def withTemporaryDoubleVariables[A](n: Int)(f: PartialFunction[List[alloc.Var], A]): A =
    val vs = (1 to n).toList.map(i => alloc.addDoubleVariable(s"$$temporary_$i", apronState, ApronAllocationSite.TemporaryVar))
    try f(vs)
    finally {
      vs.foreach(alloc.freeVariable(_, apronState))
    }

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
    setLeastExtendingEnvironment(snapshot)
    val state1 = apronState
    apronState = new Abstract1(apronManager, snapshot)
    val res2 = TrySturdy(constrainPure(condFalse)(ifFalse))

    (res1.isBottom, res2.isBottom) match
      case (false, false) =>
        setLeastExtendingEnvironment(state1)
        if (widen)
          apronState = state1.widening(apronManager, apronState)
        else {
          state1.join(apronManager, apronState)
          apronState = state1
        }
      case (false, true) =>
        apronState = state1
      case (true, false) =>
        // nothing
      case (true, true) =>
        apronState = snapshot

    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")
    Join(res1, res2).get.getOrThrow


  def joinValues(e1: ApronExpr, e2: ApronExpr, widen: Boolean): MaybeChanged[ApronExpr] =
    if (getBound(e1).isEqual(getBound(e2))) {
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
    new ApronState(new Abstract1(apronManager, apronState), apronManager)
  override def setState(as: ApronState): Unit =
    val st = new Abstract1(apronManager, as.s)
    setLeastExtendingEnvironment(st)
    apronState = st
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
      val lce = s1.getEnvironment.lce(s2.getEnvironment)
      val state1 = s1.changeEnvironmentCopy(apronManager, lce, false)
      val state2 = s2.changeEnvironmentCopy(apronManager, lce, false)
      state2.join(apronManager, state1)
      if (state2.isBottom(apronManager))
        throw new SturdyFailure {}
      val changed = !state2.isEqual(apronManager, state1)
      MaybeChanged(new ApronState(state2, apronManager), changed)
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
      val lce = s1.getEnvironment.lce(s2.getEnvironment)
      val state1 = s1.changeEnvironmentCopy(apronManager, lce, false)
      val state2 = s2.changeEnvironmentCopy(apronManager, lce, false)
      val widened = state1.widening(apronManager, state2)
      if (widened.isBottom(apronManager))
        throw new SturdyFailure {}
      val changed = !widened.isEqual(apronManager, state1)
      if (debugWiden && changed && apronState.toString(apronManager) == state1.toString(apronManager))
        throw new IllegalStateException()
      if (debugWiden) {
        println(
          s"""Widening apron
             |  s1 = $s1
             |  s2 = $s2
             |  widened = $widened
             |  changed = $changed""".stripMargin)
      }
      MaybeChanged(new ApronState(widened, apronManager), changed)
    }
  }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A] {
    private val snapshot = new Abstract1(apronManager, apronState)
    private var fState: Abstract1 = _

    override def inbetween(): Unit =
      setLeastExtendingEnvironment(snapshot)
      fState = apronState
      apronState = new Abstract1(apronManager, snapshot)

    override def retainNone(): Unit =
      apronState = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      apronState = fState

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      setLeastExtendingEnvironment(fState)
      fState.join(apronManager, apronState)
      apronState = fState
      if (apronState.isBottom(apronManager))
        throw new SturdyFailure {}
  })

class ApronState(val s: Abstract1, val apronManager: Manager):
  override def equals(obj: Any): Boolean = obj match
    case other: ApronState =>
      val lce = s.getEnvironment.lce(other.s.getEnvironment)
      val s1 = s.changeEnvironmentCopy(apronManager, lce, false)
      val s2 = other.s.changeEnvironmentCopy(apronManager, lce, false)
      s1.isEqual(apronManager, s2)
    case _ => false

  override def hashCode(): Int =
    s.hashCode(apronManager)

  override def toString: String = 
    "(env = " + s.getEnvironment.toString + ", state = " + s.toString(apronManager) + ")"

