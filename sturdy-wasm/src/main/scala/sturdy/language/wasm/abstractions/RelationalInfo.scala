package sturdy.language.wasm.abstractions

import apron.Interval
import sturdy.apron.{*, given}
import sturdy.values.{*,given}
import sturdy.values.references.{*,given}
import sturdy.values.MaybeChanged.{Changed, Unchanged}

import scala.collection.immutable.List as Type

object RelationalInfo:
  enum IsConstrained:
    case Constrained
    case Unconstrained

  given PartialOrder[IsConstrained] with
    import IsConstrained.*

    override def tryCompare(x: IsConstrained, y: IsConstrained): Option[Int] =
      (x, y) match
        case (Constrained, Constrained) | (Unconstrained, Unconstrained) => Some(0)
        case (Constrained, Unconstrained) => Some(-1)
        case (Unconstrained, Constrained) => Some(1)

    override def lteq(x: IsConstrained, y: IsConstrained): Boolean =
      tryCompare(x, y).get <= 0

  enum Info:
    case Numeric(interval: Interval, tpe: Any, constrained: IsConstrained)
    case Boolean(value: Topped[scala.Boolean], constrained: IsConstrained)
    case AllocationSites(sites: AbstractReference[Powerset[PhysicalAddress[Any]]], size: Interval, sizeConstrained: IsConstrained)
    case Top

    def isConstrained: scala.Boolean =
      this match
        case Numeric(_, _, isConstrained) => isConstrained == IsConstrained.Constrained
        case Boolean(_, isConstrained) => isConstrained == IsConstrained.Constrained
        case AllocationSites(_, _, isConstrained) => isConstrained == IsConstrained.Constrained
        case Top => true

    inline def isUnconstrained: scala.Boolean = !isConstrained

  given PartialOrder[Info] with
    import Info.*

    override def lteq(x: Info, y: Info): scala.Boolean =
      (x, y) match
        case (Numeric(iv1, _, isConstrained1), Numeric(iv2, _, isConstrained2)) =>
          PartialOrder.lteq((isConstrained1, iv1), (isConstrained2, iv2))
        case (Boolean(toppedBool1, isConstrained1), Boolean(toppedBool2, isConstrained2)) =>
          PartialOrder.lteq((isConstrained1, toppedBool1), (isConstrained2, toppedBool2))
        case (AllocationSites(sites1, iv1, isConstrained1), AllocationSites(sites2, iv2, isConstrained2)) =>
          PartialOrder.lteq((isConstrained1, iv1, sites1), (isConstrained2, iv2, sites2))
        case (_: Boolean, _: Numeric) => true
        case (_: AllocationSites, _: Numeric) => true
        case (_, Top) => true
        case _ => false

  given joinIsConstrained: Join[IsConstrained] with
    import IsConstrained.*
    override def apply(v1: IsConstrained, v2: IsConstrained): MaybeChanged[IsConstrained] =
      (v1, v2) match
        case (Constrained, Constrained) => Unchanged(Constrained)
        case (Constrained, Unconstrained) => Changed(Unconstrained)
        case (Unconstrained, _) => Unchanged(Unconstrained)

  given Join[Info] = {
    case (Info.Numeric(iv1, tpe1, constrained1), Info.Numeric(iv2, tpe2, constrained2)) if tpe1 == tpe2 =>
      for {
        iv <- Join(iv1, iv2)
        constrained <- joinIsConstrained(constrained1, constrained2)
      } yield (Info.Numeric(iv, tpe1, constrained))
    case (Info.Boolean(b1, constrained1), Info.Boolean(b2, constrained2)) =>
      for {
        b <- Join(b1, b2)
        constrained <- joinIsConstrained(constrained1, constrained2)
      } yield (Info.Boolean(b, constrained))
    case (Info.AllocationSites(ref1, size1, constrained1), Info.AllocationSites(ref2, size2, constrained2)) =>
      for {
        sites <- Join(ref1, ref2)
        size <- Join(size1, size2)
        constrained <- joinIsConstrained(constrained1, constrained2)
      } yield (Info.AllocationSites(sites, size, constrained))
    case (Info.Top, _) => Unchanged(Info.Top)
    case (_, Info.Top) => Changed(Info.Top)
    case (_, _) => Changed(Info.Top)
  }
