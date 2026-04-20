package sturdy.values.booleans

import sturdy.effect.EffectStack
import sturdy.effect.failure.{AssertionFailure, Failure}
import sturdy.values.{Join, Widen}

import scala.annotation.targetName


/** Selects a value depending on the given Boolean */
trait BooleanSelection[B, R]:
  def boolSelect(v: B, ifTrue: R, ifFalse: R): R
object BooleanSelection:
  def apply[B, R](v: B, ifTrue: R, ifFalse: R)(using ops: BooleanSelection[B, R]): R =
    ops.boolSelect(v, ifTrue, ifFalse)
  given throughBranching[B, R](using br: BooleanBranching[B, R]): BooleanSelection[B, R] with
    override def boolSelect(v: B, ifTrue: R, ifFalse: R): R = br.boolBranch(v, ifTrue, ifFalse)

given ConcreteBooleanSelection[R]: BooleanSelection[Boolean, R] with
  def boolSelect(v: Boolean, ifTrue: R, ifFalse: R): R =
    if (v) ifTrue else ifFalse

/** Executes a branch depending on the given Boolean */
trait BooleanBranching[B, R]:
  def boolBranch(v: B, thn: => R, els: => R): R

  @targetName("boolBranchSplit")
  inline final def boolBranch(v: B)(thn: => R)(els: => R): R =
    boolBranch(v, thn, els)

given ConcreteBooleanBranching[R]: BooleanBranching[Boolean, R] with
  def boolBranch(v: Boolean, thn: => R, els: => R): R =
    if (v) thn else els

class ObservedBooleanBranching[B, R](using ops: BooleanBranching[B, R]) extends BooleanBranching[B, R]:
  private var observer: List[B => Unit] = List()
  def addObserver(f: B => Unit): Unit = observer +:= f

  override def boolBranch(v: B, thn: => R, els: => R): R =
//    val f = observer.head
//    f(v)
//    observer = observer.tail
    ops.boolBranch(v, thn, els)


trait BreakIf[B]:
  type State
  def break(br: State => Unit): Unit
  def breakIf(cond: B)(break: State => Unit): Unit
  def assertCondition(cond: B, state: State): Unit
  def joinClosingOver[Body](using Join[Body]): Join[(Body,State)]
  def widenClosingOver[Body](using Widen[Body]): Widen[(Body,State)]

given ConcreteBreakIf(using failure: Failure, effectStack: EffectStack): BreakIf[Boolean] with
  type State = effectStack.State

  override def break(br: State => Unit): Unit =
    br(effectStack.getState)

  override def breakIf(cond: Boolean)(break: State => Unit): Unit =
    if(cond) {
      break(effectStack.getState)
    }

  override def assertCondition(cond: Boolean, state: State): Unit =
    effectStack.setState(state)
    if(!cond)
      failure.fail(AssertionFailure(cond), s"Expected condition to be true, but the condition was $cond")

  override def joinClosingOver[Body](using Join[Body]): Join[(Body, Any)] = throw UnsupportedOperationException()
  override def widenClosingOver[Body](using Widen[Body]): Widen[(Body, Any)] = throw UnsupportedOperationException()
