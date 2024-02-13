package sturdy.values.booleans

import sturdy.data.NoJoin

import scala.annotation.targetName


/** Selects a value depending on the given Boolean */
trait BooleanSelection[B, R]:
  def boolSelect(v: B, ifTrue: R, ifFalse: R): R
object BooleanSelection:
  def apply[B, R](v: B, ifTrue: R, ifFalse: R)(using ops: BooleanSelection[B, R]): R =
    ops.boolSelect(v, ifTrue, ifFalse)
  given throughBranching[B, R](using br: BooleanBranching[B, R]): BooleanSelection[B, R] with
    override def boolSelect(v: B, ifTrue: R, ifFalse: R): R = br.boolBranch(v, ifTrue, ifFalse)

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
