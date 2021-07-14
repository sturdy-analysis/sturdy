package sturdy.values.branch

import sturdy.values.JoinValue
import sturdy.values.Topped
import sturdy.values.Topped._

trait BranchOps[V]:
  type BranchJoin[A]
  def if_[A](v: V, thn: => A, els: => A)(using BranchJoin[A]): A

given ConcreteBranchOps: BranchOps[Boolean] with
//  type BranchJoin[A] = Unit
  def if_[A](v: Boolean, thn: => A, els: => A)(using BranchJoin[A]): A =
    if v then thn else els

given ToppedBooleanBranchOps: BranchOps[Topped[Boolean]] with
  type BranchJoin[A] = JoinValue[A]
  def if_[A](v: Topped[Boolean], thn: => A, els: => A)(using j: BranchJoin[A]): A = v match
    case Actual(true) => thn
    case Actual(false) => els
    case Top => j.joinValues(thn, els)
