package sturdy.values.booleans

import sturdy.data.NoJoin

import scala.annotation.targetName

trait BooleanBranching[V, J[_]]:
  def boolBranch[A](v: V, thn: => A, els: => A): J[A] ?=> A

  @targetName("boolBranchSplit")
  inline final def boolBranch[A](v: V)(thn: => A)(els: => A): J[A] ?=> A =
    boolBranch(v, thn, els)

given ConcreteBooleanBranching: BooleanBranching[Boolean, NoJoin] with
  type BoolBranchJoin[A] = Unit
  def boolBranch[A](v: Boolean, thn: => A, els: => A): BoolBranchJoin[A] ?=> A =
    if (v) thn else els
