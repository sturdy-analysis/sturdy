package sturdy.effect.branching

trait BoolBranching[V]:
  type BoolBranchJoin[A]
  final type BoolBranchJoined[A] = BoolBranchJoin[A] ?=> A

  def boolBranch[A](v: V, thn: => A, els: => A): BoolBranchJoined[A]
