package sturdy.effect.branching

import scala.annotation.targetName

trait BoolBranching[V]:
  type BoolBranchJoin[A]
  final type BoolBranchJoined[A] = BoolBranchJoin[A] ?=> A

  def boolBranch[A](v: V, thn: => A, els: => A): BoolBranchJoined[A]

  @targetName("boolBranchSplit")
  inline final def boolBranch[A](v: V)(thn: => A)(els: => A): BoolBranchJoined[A] =
    boolBranch(v, thn, els)
