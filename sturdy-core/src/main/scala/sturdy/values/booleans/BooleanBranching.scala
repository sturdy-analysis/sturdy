package sturdy.values.booleans

import sturdy.data.NoJoin

import scala.annotation.targetName

trait BooleanBranching[B, R]:
  def boolBranch(v: B, thn: => R, els: => R): R

  @targetName("boolBranchSplit")
  inline final def boolBranch(v: B)(thn: => R)(els: => R): R =
    boolBranch(v, thn, els)

given ConcreteBooleanBranching[R]: BooleanBranching[Boolean, R] with
  def boolBranch(v: Boolean, thn: => R, els: => R): R =
    if (v) thn else els
