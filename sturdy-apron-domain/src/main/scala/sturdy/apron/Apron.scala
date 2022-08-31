package sturdy.apron

import apron.{Abstract1, Environment, Interval, Linexpr1, Manager, MpqScalar, StringVar, Tcons0, Tcons1, Texpr0UnNode, Texpr1BinNode, Texpr1CstNode, Texpr1Intern, Texpr1Node, Texpr1UnNode, Texpr1VarNode, Var as ApronVar}
import sturdy.data.CombineUnit
import sturdy.effect.{CombineTrySturdy, EffectStack, SturdyFailure, TrySturdy}
import sturdy.values.{Combine, Join, MaybeChanged, Topped, Widen, Widening}

import java.lang.IllegalStateException

val APRON_VAR_COUNT_LIMIT = 10

class Apron(val apronManager: Manager, val alloc: ApronAlloc):
  override def toString: String =
    apronEnv.getVars.mkString("Array(", ", ", ") : ") + apronState.toString(apronManager)

  var apronState: Abstract1 = new Abstract1(apronManager, new Environment())
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

  // private val topIntVar: ApronVar = new StringVar("topInt")
  def topInt: Texpr1Node = freshConstraintVariable("topInt")
  /*
    if (!apronEnv.hasVar(topIntVar)) {
      apronEnv = apronEnv.add(Array[ApronVar](topIntVar), Array.empty[ApronVar])
      apronState.changeEnvironment(apronManager, apronEnv, false)
    }
    new Texpr1VarNode(topIntVar)
  */

  def addDoubleVariable(name: String): ApronVar =
    alloc.addDoubleVariable(name, apronState)

  def addIntVariable(name: String): ApronVar =
    alloc.addIntVariable(name, apronState)

  def makeConstraint(c: Tcons0): Unit =
    new Tcons1(apronEnv, c)

  def makeConstraint(v: Texpr1Node, relOp: Int): Tcons1 =
    new Tcons1(apronEnv, relOp, v)

  def makeConstantConstraint(b: Boolean): Tcons1 =
    new Tcons1(apronEnv, Tcons1.EQ, Texpr1CstNode(MpqScalar(if (b) 0 else 1)))

  def constrain(v: Texpr1Node, relOp: Int): Unit =
    constrain(makeConstraint(v, relOp))

  def constrain(c: Tcons1): Unit =
    if (apronState.isBottom(apronManager))
      throw new IllegalStateException(s"Apron state may not be bottom prior to constraining!")
    c.extendEnvironment(apronEnv)
    apronState.meet(apronManager, c)
    if (apronState.isBottom(apronManager))
      throw new SturdyFailure {}

  def assign(name: String, expr: Linexpr1): Unit =
    // TODO ???
    // strong update: overwrite old value
    // weak update: join with old
    apronState.assign(apronManager, name, expr, null)

  def freshConstraintVariable(purpose: String): Texpr1VarNode =
    val newApronVar = alloc.addIntVariable(purpose, apronState)
    new Texpr1VarNode(newApronVar)

  def joinDISEQ[A](cond: Tcons1, block: => A)(using effects: EffectStack): Join[A] ?=> A =
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

    Join(res1, res2).get.getOrThrow


  def negateExpr(cond : Tcons1) : Tcons1 = cond.getKind match
    case Tcons1.EQ => new Tcons1(cond.getEnvironment, Tcons1.DISEQ, cond.toTexpr1Node)
    case Tcons1.DISEQ => new Tcons1(cond.getEnvironment, Tcons1.EQ, cond.toTexpr1Node)
    case Tcons1.SUP => new Tcons1(cond.getEnvironment, Tcons1.SUPEQ, Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node))
    case Tcons1.SUPEQ => new Tcons1(cond.getEnvironment, Tcons1.SUP, Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node))
    case Tcons1.EQMOD => ??? // not useful

  def joinValues(v1: Texpr1Node, v2: Texpr1Node, widen: Boolean): MaybeChanged[Texpr1Node] =
    val x = freshConstraintVariable(if (widen) "widen" else "join")
    val v1Cons = makeConstraint(new Texpr1BinNode(Texpr1BinNode.OP_SUB, x, v1), Tcons1.EQ)
    val v2Cons = makeConstraint(new Texpr1BinNode(Texpr1BinNode.OP_SUB, x, v2), Tcons1.EQ)
    ifThenElsePure(v1Cons, v2Cons, widen)(())(())

    val xBound = getBound(x)
    val v1Bound = getBound(v1)
    MaybeChanged(x, !xBound.isEqual(v1Bound))


given JoinTexpr1Node(using ap: Apron): Join[Texpr1Node] with
  def apply(v1: Texpr1Node, v2: Texpr1Node): MaybeChanged[Texpr1Node] =
    ap.joinValues(v1, v2, widen = false)

given WideningTexpr1Node(using ap: Apron): Widen[Texpr1Node] with
  def apply(v1: Texpr1Node, v2: Texpr1Node): MaybeChanged[Texpr1Node] =
    ap.joinValues(v1, v2, widen = true)
