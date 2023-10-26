package sturdy.values.references

import sturdy.effect.failure.Failure
import sturdy.util.Label
import sturdy.values.Finite
import sturdy.values.Join
import sturdy.values.Structural
import sturdy.values.relational.EqOps
import sturdy.values.Topped

enum AllocationSiteAddr extends AbstractAddr[AllocationSiteAddr]:
  case Alloc(lab: Label)
  case AllocRelative(lab: Label, name: String)
  case Variable(name: String)

  override def toString: String = this match
    case AllocationSiteAddr.Alloc(l) => s"alloc-$l"
    case AllocationSiteAddr.AllocRelative(l, name) => s"alloc-$l-$name"
    case AllocationSiteAddr.Variable(name) => s"&$name"

  override def isEmpty: Boolean = false
  override def isStrong: Boolean = false
  override def reduce[A](f: AllocationSiteAddr => A)(using Join[A]): A = f(this)
  override def iterator: Iterator[AllocationSiteAddr] = Iterator(this)

given Finite[AllocationSiteAddr] with {}
given Structural[AllocationSiteAddr] with {}

given EqOps[AllocationSiteAddr, Topped[Boolean]] with
  override def equ(v1: AllocationSiteAddr, v2: AllocationSiteAddr): Topped[Boolean] =
    if (v1 != v2)
      Topped.Actual(false)
    else
      Topped.Top
  override def neq(v1: AllocationSiteAddr, v2: AllocationSiteAddr): Topped[Boolean] =
    if (v1 != v2)
      Topped.Actual(true)
    else
      Topped.Top
