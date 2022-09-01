package sturdy.apron

import apron.{Texpr0UnNode, Environment, Texpr1CstNode, Texpr1UnNode, Interval, MpqScalar, Texpr1VarNode, Linexpr1, Texpr1Node, Tcons0, Tcons1, StringVar, Texpr1BinNode, Texpr1Intern, Manager, Abstract1, Var as ApronVar}
import sturdy.data.CombineUnit
import sturdy.effect.ComputationJoiner
import sturdy.effect.Stateful
import sturdy.effect.{EffectStack, SturdyFailure, CombineTrySturdy, TrySturdy}
import sturdy.values.{Combine, MaybeChanged, Widening, Topped, Join, Widen}

import java.lang.{IllegalStateException, IllegalArgumentException}

val APRON_VAR_COUNT_LIMIT = 10

class Apron(val apronManager: Manager, val alloc: ApronAlloc) extends Stateful:
  override def toString: String =
    apronEnv.getVars.mkString("Array(", ", ", ") : ") + apronState.toString(apronManager)

  private var apronState: Abstract1 = new Abstract1(apronManager, new Environment())

  def apronEnv: Environment = apronState.getEnvironment

  def setLeastExtendingEnvironment(other: Abstract1): Unit = {
    val lce = apronEnv.lce(other.getEnvironment)
    apronState.changeEnvironment(apronManager, lce, false)
    other.changeEnvironment(apronManager, lce, false)
  }

  def getBound(v: Topped[Texpr1Node]): Interval = v match
    case Topped.Top => Interval(Int.MinValue, Int.MaxValue) // TODO check if sound
    case Topped.Actual(v) =>
      val vIntern = new Texpr1Intern(apronEnv, v)
      apronState.getBound(apronManager, vIntern)


  def topInt: Topped[Texpr1Node] = Topped.Top

  def addDoubleVariable(name: String): ApronVar =
    alloc.addDoubleVariable(name, apronState)

  def addIntVariable(name: String): ApronVar =
    alloc.addIntVariable(name, apronState)

  def makeConstraint(c: Tcons0): Unit =
    new Tcons1(apronEnv, c)

  def makeConstraint(v: Texpr1Node, relOp: Int): Topped[Tcons1] =
    Topped.Actual(Tcons1(apronEnv, relOp, v))

  def makeConstraint(v: Topped[Texpr1Node], relOp: Int): Topped[Tcons1] = v match
    case Topped.Top => Topped.Top
    case Topped.Actual(v) => Topped.Actual(Tcons1(apronEnv, relOp, v))

  def makeConstantConstraint(b: Boolean): Topped[Tcons1] =
    Topped.Actual(Tcons1(apronEnv, Tcons1.EQ, Texpr1CstNode(MpqScalar(if (b) 0 else 1))))

  def constrain(v: Topped[Texpr1Node], relOp: Int): Unit =
    constrain(makeConstraint(v, relOp))

  def constrain(c: Topped[Tcons1]): Unit = c match
    case Topped.Actual(c) =>
      if (apronState.isBottom(apronManager))
        throw new IllegalStateException(s"Apron state may not be bottom prior to constraining!")
      if(c.getKind == Tcons1.DISEQ)
        throw new IllegalArgumentException("DISEQ constraints should be handled outside of the function!")
      c.extendEnvironment(apronEnv)
      apronState.meet(apronManager, c)
      if (apronState.isBottom(apronManager))
        throw new SturdyFailure {}
    case _ => ()

  def constrain(v: Texpr1Node, relOp: Int): Unit =
    constrain(makeConstraint(v, relOp))

  def constrain(c: Tcons1): Unit = constrain(Topped.Actual(c))

  def assign(v: ApronVar, exp: Texpr1Node): Unit =
    // TODO ???
    // strong update: overwrite old value
    // weak update: join with old
    val expIntern = new Texpr1Intern(apronEnv, exp)
    apronState.assign(apronManager, v, expIntern, null)
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")

  def assign(v: ApronVar, exp: Linexpr1): Unit =
    // TODO ???
    // strong update: overwrite old value
    // weak update: join with old
    apronState.assign(apronManager, v, exp, null)
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")

  def freshConstraintVariable(purpose: String): Topped[Texpr1VarNode] =
    val newApronVar = alloc.addIntVariable(purpose, apronState)
    Topped.Actual(Texpr1VarNode(newApronVar))

  def joinDISEQ[A](cond: Tcons1, block: => A)(using effects: EffectStack): Join[A] ?=> A =
    val supCond = Topped.Actual(Tcons1(cond.getEnvironment, Tcons1.SUP, cond.toTexpr1Node))
    val infCond = Topped.Actual(Tcons1(cond.getEnvironment, Tcons1.SUP, Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node)))
    effects.joinComputations {
      constrain(supCond)
      block
    } {
      constrain(infCond)
      block
    }

  private def ifThenElse[A](cond: Tcons1)(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A =
    effects.joinComputations {
      cond.getKind match
        case Tcons1.DISEQ => joinDISEQ(cond, ifTrue)
        case _ =>
          constrain(Topped.Actual(cond))
          ifTrue
    } {
      val notCond = negateExpr(cond)
      notCond.getKind match
        case Tcons1.DISEQ => joinDISEQ(notCond, ifFalse)
        case _ =>
          constrain(Topped.Actual(notCond))
          ifFalse
    }

  def ifThenElse[A](cond: Topped[Tcons1])(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A = cond match
    case Topped.Top => effects.joinComputations(ifTrue)(ifFalse)
    case Topped.Actual(b) => ifThenElse(b)(ifTrue)(ifFalse)

  def ifThenElsePureDISEQ[A](cond: Tcons1, widen: Boolean, value: A): Join[A] ?=> A =
    val supCond = Topped.Actual(Tcons1(cond.getEnvironment, Tcons1.SUP, cond.toTexpr1Node))
    val infCond = Topped.Actual(Tcons1(cond.getEnvironment, Tcons1.SUP, Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node)))
    ifThenElsePure(supCond, infCond, widen)(value)(value)

  def ifThenElsePure[A](condTrue: Topped[Tcons1], widen: Boolean = true)(ifTrue: A)(ifFalse: A): Join[A] ?=> A =
    ifThenElsePure(condTrue, negateExpr(condTrue), widen)(ifTrue)(ifFalse)

  def ifThenElsePure[A](condTrue: Topped[Tcons1], condFalse: Topped[Tcons1], widen: Boolean)(ifTrue: A)(ifFalse: A): Join[A] ?=> A =
    val snapshot = new Abstract1(apronManager, apronState)
    val res1 =
      if condTrue.isActual && condTrue.get.getKind == Tcons1.DISEQ then
        TrySturdy {ifThenElsePureDISEQ(condTrue.get, widen, ifTrue)}
      else
        TrySturdy {
          constrain(condTrue)
          ifTrue
        }
    setLeastExtendingEnvironment(snapshot)
    val state1 = apronState
    apronState = snapshot
    val res2 =
      if condFalse.isActual && condFalse.get.getKind == Tcons1.DISEQ then
        TrySturdy {ifThenElsePureDISEQ(condTrue.get, widen, ifTrue)}
      else
        TrySturdy {
          constrain(condFalse)
          ifFalse
        }
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

  private def negateExpr(cond : Tcons1): Tcons1 = negateExpr(Topped.Actual(cond)).get

  def negateExpr(cond : Topped[Tcons1]) : Topped[Tcons1] = cond match
    case Topped.Top => Topped.Top
    case Topped.Actual(cond) => cond.getKind match
      case Tcons1.EQ => Topped.Actual(Tcons1(cond.getEnvironment, Tcons1.DISEQ, cond.toTexpr1Node))
      case Tcons1.DISEQ => Topped.Actual(Tcons1(cond.getEnvironment, Tcons1.EQ, cond.toTexpr1Node))
      case Tcons1.SUP => Topped.Actual(Tcons1(cond.getEnvironment, Tcons1.SUPEQ, Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node)))
      case Tcons1.SUPEQ => Topped.Actual(Tcons1(cond.getEnvironment, Tcons1.SUP, Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node)))
      case Tcons1.EQMOD => ??? // not useful

  def joinValues(v1: Texpr1Node, v2: Texpr1Node, widen: Boolean): MaybeChanged[Texpr1Node] =
    val x = freshConstraintVariable(if (widen) "widen" else "join").get
    val v1Cons = makeConstraint(Topped.Actual(Texpr1BinNode(Texpr1BinNode.OP_SUB, x, v1)), Tcons1.EQ)
    val v2Cons = makeConstraint(Topped.Actual(Texpr1BinNode(Texpr1BinNode.OP_SUB, x, v2)), Tcons1.EQ)
    ifThenElsePure(v1Cons, v2Cons, widen)(())(())

    val xBound = getBound(Topped.Actual(x))
    val v1Bound = getBound(Topped.Actual(x))
    MaybeChanged(x, !xBound.isEqual(v1Bound))

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
      if (changed && apronState.toString(apronManager) == state1.toString(apronManager))
        throw new IllegalStateException()
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
      if (changed && apronState.toString(apronManager) == state1.toString(apronManager))
        throw new IllegalStateException()
      MaybeChanged(new ApronState(widened, apronManager), changed)
    }
  }

  def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoiner[A] {
    private val snapshot = new Abstract1(apronManager, apronState)
    private var fState: Abstract1 = _

    override def inbetween(): Unit =
      setLeastExtendingEnvironment(snapshot)
      fState = apronState
      apronState = snapshot

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
  }

class ApronState(val s: Abstract1, apronManager: Manager):
  override def equals(obj: Any): Boolean = obj match
    case other: ApronState => s.isEqual(apronManager, other.s)
    case _ => false

  override def hashCode(): Int =
    s.hashCode(apronManager)

  override def toString: String = s.toString(apronManager)

given JoinToppedTexpr1Node(using ap: Apron, j: Join[Texpr1Node]): Join[Topped[Texpr1Node]] with
  override def apply(v1: Topped[Texpr1Node], v2: Topped[Texpr1Node]): MaybeChanged[Topped[Texpr1Node]] = (v1,v2) match
    case (Topped.Top, _) => MaybeChanged.Unchanged(Topped.Top)
    case (_, Topped.Top) => MaybeChanged.Changed(Topped.Top)
    case (Topped.Actual(a), Topped.Actual(b)) => j(a,b).map(Topped.Actual(_))

given WideningToppedTexpr1Node(using ap: Apron, j: Widen[Texpr1Node]): Widen[Topped[Texpr1Node]] with
  override def apply(v1: Topped[Texpr1Node], v2: Topped[Texpr1Node]): MaybeChanged[Topped[Texpr1Node]] = (v1,v2) match
    case (Topped.Top, _) => MaybeChanged.Unchanged(Topped.Top)
    case (_, Topped.Top) => MaybeChanged.Changed(Topped.Top)
    case (Topped.Actual(a), Topped.Actual(b)) => j(a,b).map(Topped.Actual(_))

given JoinTexpr1Node(using ap: Apron): Join[Texpr1Node] with
  def apply(v1: Texpr1Node, v2: Texpr1Node): MaybeChanged[Texpr1Node] =
    ap.joinValues(v1, v2, widen = false)

given WideningTexpr1Node(using ap: Apron): Widen[Texpr1Node] with
  def apply(v1: Texpr1Node, v2: Texpr1Node): MaybeChanged[Texpr1Node] =
    ap.joinValues(v1, v2, widen = true)
