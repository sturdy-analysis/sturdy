package sturdy.apron

import apron.{Texpr0UnNode, Environment, Texpr1CstNode, Texpr1UnNode, Interval, MpqScalar, Texpr1VarNode, Linexpr1, Texpr1Node, Tcons0, Tcons1, StringVar, Texpr1BinNode, Texpr1Intern, Manager, Abstract1, Var as ApronVar}
import sturdy.data.CombineUnit
import sturdy.effect.ComputationJoiner
import sturdy.effect.Stateful
import sturdy.effect.{EffectStack, SturdyFailure, CombineTrySturdy, TrySturdy}
import sturdy.values.{Combine, MaybeChanged, Widening, Topped, Join, Widen}

import java.lang.IllegalStateException

val APRON_VAR_COUNT_LIMIT = 10

class Apron(val apronManager: Manager, val alloc: ApronAlloc) extends Stateful:
  override def toString: String =
    apronEnv.getVars.mkString("Array(", ", ", ") : ") + apronState.toString(apronManager)

  def printlndebug(s: String) = {} // println(s)
  
  private var apronState: Abstract1 = new Abstract1(apronManager, new Environment())
  /** global var count, currently unbounded */
  private var _apronVarCount: Int = 0

  def apronEnv: Environment = apronState.getEnvironment

  def setLeastExtendingEnvironment(other: Abstract1): Unit = {
    val lce = apronEnv.lce(other.getEnvironment)
    apronState.changeEnvironment(apronManager, lce, false)
    other.changeEnvironment(apronManager, lce, false)
  }

  def getBound(v: Texpr1Node): Interval =
    val vIntern = new Texpr1Intern(apronEnv, v)
    apronState.getBound(apronManager, vIntern)

  private val topIntVar: ApronVar = addIntVariable("topInt")
  def topInt: Texpr1Node = new Texpr1VarNode(topIntVar)
  /*
    if (!apronEnv.hasVar(topIntVar)) {
      apronEnv = apronEnv.add(Array[ApronVar](topIntVar), Array.empty[ApronVar])
      apronState.changeEnvironment(apronManager, apronEnv, false)
    }
    new Texpr1VarNode(topIntVar)
  */

  def addDoubleVariable(name: String): ApronVar =
    printlndebug(s"addDvar($name)")
    alloc.addDoubleVariable(name, apronState)

  def addIntVariable(name: String): ApronVar =
    printlndebug(s"addIvar($name)")
    alloc.addIntVariable(name, apronState)

  def makeConstraint(c: Tcons0): Unit =
    printlndebug(s"makeConstraint($c)")
    new Tcons1(apronEnv, c)

  def makeConstraint(v: Texpr1Node, relOp: Int): Tcons1 =
    printlndebug(s"makeConstraint($v, $relOp)")
    new Tcons1(apronEnv, relOp, v)

  def makeConstantConstraint(b: Boolean): Tcons1 =
    printlndebug(s"makeConstraint($b)")
    new Tcons1(apronEnv, Tcons1.EQ, Texpr1CstNode(MpqScalar(if (b) 0 else 1)))

  def constrain(v: Texpr1Node, relOp: Int): Unit =
    constrain(makeConstraint(v, relOp))

  def constrain(c: Tcons1): Unit =
    printlndebug(s"constrain($c)")
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"Apron state may not be bottom prior to constraining!")
    c.extendEnvironment(apronEnv)
    apronState.meet(apronManager, c)
    if (apronState.isBottom(apronManager))
      throw new SturdyFailure {}

  def assign(v: ApronVar, exp: Texpr1Node): Unit =
    printlndebug(s"assign($v, $exp)")
    // TODO ???
    // strong update: overwrite old value
    // weak update: join with old
    val expIntern = new Texpr1Intern(apronEnv, exp)
    apronState.assign(apronManager, v, expIntern, null)
    printlndebug(s"assign($v, $exp) done")
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")
    printlndebug(s"assign($v, $exp) bottom done")

  def assign(v: ApronVar, exp: Linexpr1): Unit =
    printlndebug(s"assign($v, $exp)")
    // TODO ???
    // strong update: overwrite old value
    // weak update: join with old
    apronState.assign(apronManager, v, exp, null)
    printlndebug(s"assign($v, $exp) done")
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"bottom state illegal here")
    printlndebug(s"assign($v, $exp) bottom done")

  def freshConstraintVariable(purpose: String): Texpr1VarNode =
    printlndebug(s"fresh($purpose)")
    val newApronVar = alloc.addIntVariable(purpose, apronState)
    new Texpr1VarNode(newApronVar)

  def joinDISEQ[A](cond: Tcons1, block: => A)(using effects: EffectStack): Join[A] ?=> A =
    printlndebug(s"joinDISEQ($cond)")
    val supCond = new Tcons1(cond.getEnvironment, Tcons1.SUP, cond.toTexpr1Node)
    val infCond = new Tcons1(cond.getEnvironment, Tcons1.SUP, Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node))
    effects.joinComputations {
      constrain(supCond)
      block
    } {
      constrain(infCond)
      block
    }

  def ifThenElse[A](cond: Topped[Tcons1])(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A = cond match
    case Topped.Top => effects.joinComputations(ifTrue)(ifFalse)
    case Topped.Actual(b) => ifThenElse(b)(ifTrue)(ifFalse)

  def ifThenElse[A](cond: Tcons1)(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A =
    printlndebug(s"ifThenElse($cond)")
    effects.joinComputations {
      cond.getKind match
        case Tcons1.DISEQ => joinDISEQ(cond, ifTrue)
        case _ =>
          constrain(cond)
          ifTrue

    } {
      val notCond = negateExpr(cond)
      notCond.getKind match
        case Tcons1.DISEQ => joinDISEQ(notCond, ifFalse)
        case _ =>
          constrain(notCond)
          ifFalse
    }

  def ifThenElsePure[A](condTrue: Tcons1, widen: Boolean = true)(ifTrue: A)(ifFalse: A): Join[A] ?=> A =
    ifThenElsePure(condTrue, negateExpr(condTrue), widen)(ifTrue)(ifFalse)

  def ifThenElsePure[A](condTrue: Tcons1, condFalse: Tcons1, widen: Boolean)(ifTrue: A)(ifFalse: A): Join[A] ?=> A =
    printlndebug(s"ifThenElsePure($condTrue, $condFalse, $ifTrue, $ifFalse)")
    val snapshot = new Abstract1(apronManager, apronState)
    val res1 = TrySturdy {
      constrain(condTrue)
      ifTrue
    }
    setLeastExtendingEnvironment(snapshot)
    val state1 = apronState
    apronState = snapshot
    val res2 = TrySturdy {
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


  def negateExpr(cond : Tcons1) : Tcons1 =
    printlndebug(s"negateExpr($cond)")
    cond.getKind match
      case Tcons1.EQ => new Tcons1(cond.getEnvironment, Tcons1.DISEQ, cond.toTexpr1Node)
      case Tcons1.DISEQ => new Tcons1(cond.getEnvironment, Tcons1.EQ, cond.toTexpr1Node)
      case Tcons1.SUP => new Tcons1(cond.getEnvironment, Tcons1.SUPEQ, Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node))
      case Tcons1.SUPEQ => new Tcons1(cond.getEnvironment, Tcons1.SUP, Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node))
      case Tcons1.EQMOD => ??? // not useful

  def joinValues(v1: Texpr1Node, v2: Texpr1Node, widen: Boolean): MaybeChanged[Texpr1Node] =
    printlndebug(s"joinValues($v1, $v2, $widen)")
    val x = freshConstraintVariable(if (widen) "widen" else "join")
    val v1Cons = makeConstraint(new Texpr1BinNode(Texpr1BinNode.OP_SUB, x, v1), Tcons1.EQ)
    val v2Cons = makeConstraint(new Texpr1BinNode(Texpr1BinNode.OP_SUB, x, v2), Tcons1.EQ)
    ifThenElsePure(v1Cons, v2Cons, widen)(())(())

    val xBound = getBound(x)
    val v1Bound = getBound(v1)
    MaybeChanged(x, !xBound.isEqual(v1Bound))

  override type State = Abstract1

  override def getState: Abstract1 =
    printlndebug(s"getState()")
    new Abstract1(apronManager, apronState)
  override def setState(st: Abstract1): Unit =
    printlndebug(s"setState($st)")
    setLeastExtendingEnvironment(st)
    apronState = st
    if (apronState.isBottom(apronManager))
      throw new SturdyFailure {}
    printlndebug(s"setState($st)")

  override def join: Join[State] = (s1, s2) => {
    printlndebug(s"join($s1, $s2)")
    if (s1.isBottom(apronManager))
      MaybeChanged.Changed(s2)
    else if (s2.isBottom(apronManager))
      MaybeChanged.Unchanged(s1)
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
      MaybeChanged(state2, changed)
    }
  }

  override def widen: Widen[State] = (s1, s2) => {
    printlndebug(s"widen($s1, $s2)")
    if (s1.isBottom(apronManager))
      MaybeChanged.Changed(s2)
    else if (s2.isBottom(apronManager))
      MaybeChanged.Unchanged(s1)
    else {
      val lce = s1.getEnvironment.lce(s2.getEnvironment)
      printlndebug(s"lce = $lce")
      val state1 = s1.changeEnvironmentCopy(apronManager, lce, false)
      printlndebug(s"state1 = $state1")
      val state2 = s2.changeEnvironmentCopy(apronManager, lce, false)
      printlndebug(s"state2 = $state2")
      val widened = state1.widening(apronManager, state2)
      printlndebug(s"widened = $widened")
      if (widened.isBottom(apronManager))
        throw new SturdyFailure {}
      val changed = !widened.isEqual(apronManager, state1)
      if (changed && apronState.toString(apronManager) == state1.toString(apronManager))
        throw new IllegalStateException()
      MaybeChanged(widened, changed)
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

given JoinTexpr1Node(using ap: Apron): Join[Texpr1Node] with
  def apply(v1: Texpr1Node, v2: Texpr1Node): MaybeChanged[Texpr1Node] =
    ap.joinValues(v1, v2, widen = false)

given WideningTexpr1Node(using ap: Apron): Widen[Texpr1Node] with
  def apply(v1: Texpr1Node, v2: Texpr1Node): MaybeChanged[Texpr1Node] =
    ap.joinValues(v1, v2, widen = true)
