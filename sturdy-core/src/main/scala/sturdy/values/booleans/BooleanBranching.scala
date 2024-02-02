package sturdy.values.booleans

import sturdy.data.NoJoin

import scala.annotation.targetName


/** Selects a value depending on the given Boolean */
trait BooleanSelection[B, R]:
  def boolSelect(v: B, ifTrue: R, ifFalse: R): R
object BooleanSelection:
  def apply[B, R](v: B, ifTrue: R, ifFalse: R)(using ops: BooleanSelection[B, R]): R =
    ops.boolSelect(v, ifTrue, ifFalse)

/** Executes a branch depending on the given Boolean */
trait BooleanBranching[B, R]:
  def boolBranch(v: B, thn: => R, els: => R): R

  @targetName("boolBranchSplit")
  inline final def boolBranch(v: B)(thn: => R)(els: => R): R =
    boolBranch(v, thn, els)

given ConcreteBooleanSelection[R]: BooleanSelection[Boolean, R] with
  def boolSelect(v: Boolean, ifTrue: R, ifFalse: R): R =
    if (v) ifTrue else ifFalse

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
