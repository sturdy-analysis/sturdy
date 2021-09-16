package sturdy.effect.branching

import scala.annotation.targetName

trait BoolBranching[V]:
  type BoolBranchJoin[A]
  def boolBranch[A](v: V, thn: => A, els: => A): BoolBranchJoin[A] ?=> A

  @targetName("boolBranchSplit")
  inline final def boolBranch[A](v: V)(thn: => A)(els: => A): BoolBranchJoin[A] ?=> A =
    boolBranch(v, thn, els)
