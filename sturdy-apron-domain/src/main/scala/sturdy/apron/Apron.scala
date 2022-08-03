package sturdy.apron

import apron.{Abstract1, Environment, Interval, Linexpr1, Manager, StringVar, Tcons0, Tcons1, Texpr0UnNode, Texpr1UnNode, Texpr1BinNode, Texpr1Intern, Texpr1Node, Texpr1VarNode, Var as ApronVar}
import sturdy.data.CombineUnit
import sturdy.effect.EffectStack
import sturdy.effect.SturdyFailure
import sturdy.values.Combine
import sturdy.values.Join
import sturdy.values.MaybeChanged
import sturdy.values.Widening

class Apron(val apronManager: Manager):
  override def toString: String = apronState.toString(apronManager)

  var apronEnv: Environment = new Environment()
  var apronState: Abstract1 = new Abstract1(apronManager, apronEnv)
  /** global var count, currently unbounded */
  var apronVarCount: Int = 0


  def getBound(v: Texpr1Node): Interval =
    val vIntern = new Texpr1Intern(apronEnv, v)
    apronState.getBound(apronManager, vIntern)

  private val topIntVar: ApronVar = new StringVar("topInt")
  def topInt: Texpr1Node =
    if (!apronEnv.hasVar(topIntVar)) {
      apronEnv = apronEnv.add(Array[ApronVar](topIntVar), Array.empty[ApronVar])
      apronState.changeEnvironment(apronManager, apronEnv, false)
    }
    new Texpr1VarNode(topIntVar)

  def makeConstraint(c: Tcons0): Unit =
    new Tcons1(apronEnv, c)

  def makeConstraint(v: Texpr1Node, relOp: Int): Tcons1 =
    new Tcons1(apronEnv, relOp, v)

  def constrain(v: Texpr1Node, relOp: Int): Unit =
    constrain(makeConstraint(v, relOp))

  def constrain(c: Tcons1): Unit =
    apronState.meet(apronManager, c)
    if (apronState.isBottom(apronManager))
      throw new SturdyFailure {}

  def assign(name: String, expr: Linexpr1): Unit =
    apronState.assign(apronManager, name, expr, null)

  def freshConstraintVariable(purpose: String): Texpr1VarNode =
    val newApronVar = new StringVar(s"apronI_${apronVarCount}_$purpose")
    apronVarCount += 1
    apronEnv = apronEnv.add(Array[ApronVar](newApronVar), Array.empty[ApronVar])
    apronState.changeEnvironment(apronManager, apronEnv, false)
    new Texpr1VarNode(newApronVar)

  def ifThenElse[A](cond: Tcons1)(ifTrue: => A)(ifFalse: => A)(using effects: EffectStack): Join[A] ?=> A =
    effects.joinComputations {
      constrain(cond)
      ifTrue
    } {
      val notCond = negateExpr(cond)
      constrain(notCond)
      ifFalse
    }

  def negateExpr(cond : Tcons1) : Tcons1 = cond.getKind match
    case Tcons1.EQ => makeConstraint(cond.toTexpr1Node, Tcons1.DISEQ)
    case Tcons1.DISEQ => makeConstraint(cond.toTexpr1Node, Tcons1.EQ)
    case Tcons1.SUP => makeConstraint(Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node), Tcons1.SUPEQ)
    case Tcons1.SUPEQ => makeConstraint(Texpr1UnNode(Texpr1UnNode.OP_NEG, cond.toTexpr1Node), Tcons1.SUP)
    case Tcons1.EQMOD => ??? // not useful

given JoinTexpr1Node[W <: Widening] (using effects: EffectStack, ap: Apron): Combine[Texpr1Node, W] with
  def apply(v1: Texpr1Node, v2: Texpr1Node): MaybeChanged[Texpr1Node] =
    val x = ap.freshConstraintVariable(s"join($v1, $v2)")
    effects.joinComputations {
      ap.constrain(new Texpr1BinNode(Texpr1BinNode.OP_SUB, x, v1), Tcons1.EQ)
      ()
    } {
      ap.constrain(new Texpr1BinNode(Texpr1BinNode.OP_SUB, x, v2), Tcons1.EQ)
      ()
    }
    val xBound = ap.getBound(x)
    val v1Bound = ap.getBound(v1)
    MaybeChanged(x, xBound.isEqual(v1Bound))
