package sturdy.apron

import apron.Tcons0
import apron.Texpr0UnNode
import apron.{Environment, Interval, Texpr1VarNode, Texpr1Node, Texpr1Intern, Tcons1, StringVar, Manager, Abstract1, Var as ApronVar}
import sturdy.effect.EffectStack
import sturdy.effect.SturdyFailure
import sturdy.values.Join

trait Apron(val apronManager: Manager):
  override def toString: String = apronState.toString(apronManager)

  protected var apronEnv: Environment = new Environment()
  protected var apronState: Abstract1 = new Abstract1(apronManager, apronEnv)
  /** global var count, currently unbounded */
  protected var apronVarCount: Int = 0


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

  def freshConstraintVariable(purpose: String): Texpr1Node =
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
      val exp = cond.getTcons0.toTexpr0Node
      val negatedExp = new Texpr0UnNode(Texpr0UnNode.OP_NEG, exp)
      val notCond = new Tcons1(apronEnv, new Tcons0(cond.getKind, negatedExp))
      constrain(notCond)
      ifFalse
    }

