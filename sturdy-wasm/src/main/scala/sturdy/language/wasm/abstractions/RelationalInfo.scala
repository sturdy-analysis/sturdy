package sturdy.language.wasm.abstractions

import apron.Interval
import sturdy.apron.{*, given}
import sturdy.language.wasm.generic.FuncId
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}
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

  enum UnconstrainedInfo:
    case Numeric(interval: FloatInterval, tpe: Any, constrained: IsConstrained)
    case Boolean(value: Topped[scala.Boolean], constrained: IsConstrained)
    case GlobalAddr(sites: Powerset[(String,Long)], offset: IsConstrained)
    case StackAddr(function: Powerset[FuncId], frameSize: IsConstrained, initialOffset: Powerset[Int], otherOffset: IsConstrained)
    case HeapAddr(sites: AbstractReference[Powerset[PhysicalAddress[Any]]], size: IsConstrained, otherOffset: IsConstrained)
    case Top

    def isConstrained: scala.Boolean =
      this match
        case Numeric(_, _, isConstrained) => isConstrained == IsConstrained.Constrained
        case Boolean(_, isConstrained) => isConstrained == IsConstrained.Constrained
        case GlobalAddr(_, isConstrained) => isConstrained == IsConstrained.Constrained
        case StackAddr(_, frameSize, _, otherOffset) => frameSize == IsConstrained.Constrained && otherOffset == IsConstrained.Constrained
        case HeapAddr(_, size, otherOffset) => size == IsConstrained.Constrained && otherOffset == IsConstrained.Constrained
        case Top => true

    inline def isUnconstrained: scala.Boolean = !isConstrained

  given PartialOrder[UnconstrainedInfo] with
    import UnconstrainedInfo.*

    override def lteq(x: UnconstrainedInfo, y: UnconstrainedInfo): scala.Boolean =
      (x, y) match
        case (Numeric(iv1, _, isConstrained1), Numeric(iv2, _, isConstrained2)) =>
          PartialOrder.lteq((isConstrained1, iv1), (isConstrained2, iv2))
        case (Boolean(toppedBool1, isConstrained1), Boolean(toppedBool2, isConstrained2)) =>
          PartialOrder.lteq((isConstrained1, toppedBool1), (isConstrained2, toppedBool2))
        case (GlobalAddr(names1, offset1), GlobalAddr(names2, offset2)) =>
          PartialOrder.lteq((names1, offset1), (names2, offset2))
        case (StackAddr(functions1, frameSize1, initialOffset1, otherOffset1), StackAddr(functions2, frameSize2, initialOffset2, otherOffset2)) =>
          PartialOrder.lteq((functions1, frameSize1, initialOffset1, otherOffset1), (functions2, frameSize2, initialOffset2, otherOffset2))
        case (HeapAddr(sites1, size1, otherOffset1), HeapAddr(sites2, size2, otherOffset2)) =>
          PartialOrder.lteq((size1, otherOffset1, sites1), (size2, otherOffset2, sites2))
        case (_: Boolean, _: Numeric) => true
        case (_: GlobalAddr, _: Numeric) => true
        case (_: StackAddr, _: Numeric) => true
        case (_: HeapAddr, _: Numeric) => true
        case (_, Top) => true
        case _ => false

  given joinIsConstrained: Join[IsConstrained] with
    import IsConstrained.*
    override def apply(v1: IsConstrained, v2: IsConstrained): MaybeChanged[IsConstrained] =
      (v1, v2) match
        case (Constrained, Constrained) => Unchanged(Constrained)
        case (Constrained, Unconstrained) => Changed(Unconstrained)
        case (Unconstrained, _) => Unchanged(Unconstrained)

  given Join[UnconstrainedInfo] = {
    case (UnconstrainedInfo.Numeric(iv1, tpe1, constrained1), UnconstrainedInfo.Numeric(iv2, tpe2, constrained2)) if tpe1 == tpe2 =>
      for {
        iv <- Join(iv1, iv2)
        constrained <- joinIsConstrained(constrained1, constrained2)
      } yield (UnconstrainedInfo.Numeric(iv, tpe1, constrained))
    case (UnconstrainedInfo.Boolean(b1, constrained1), UnconstrainedInfo.Boolean(b2, constrained2)) =>
      for {
        b <- Join(b1, b2)
        constrained <- joinIsConstrained(constrained1, constrained2)
      } yield (UnconstrainedInfo.Boolean(b, constrained))
    case (UnconstrainedInfo.GlobalAddr(names1, offset1), UnconstrainedInfo.GlobalAddr(names2, offset2)) =>
      for {
        names <- Join(names1, names2)
        offset <- joinIsConstrained(offset1, offset2)
      } yield (UnconstrainedInfo.GlobalAddr(names, offset))
    case (UnconstrainedInfo.StackAddr(functions1, frameSize1, initialOffset1, otherOffset1), UnconstrainedInfo.StackAddr(functions2, frameSize2, initialOffset2, otherOffset2)) =>
      for {
        functions <- Join(functions1, functions2)
        frameSize <- joinIsConstrained(frameSize1, frameSize2)
        initialOffset <- Join(initialOffset1, initialOffset2)
        otherOffset <- joinIsConstrained(otherOffset1, otherOffset2)
      } yield (UnconstrainedInfo.StackAddr(functions, frameSize, initialOffset, otherOffset))
    case (UnconstrainedInfo.HeapAddr(ref1, size1, offset1), UnconstrainedInfo.HeapAddr(ref2, size2, offset2)) =>
      for {
        sites <- Join(ref1, ref2)
        size <- joinIsConstrained(size1, size2)
        offset <- joinIsConstrained(offset1, offset2)
      } yield (UnconstrainedInfo.HeapAddr(sites, size, offset))
    case (UnconstrainedInfo.Top, _) => Unchanged(UnconstrainedInfo.Top)
    case (_, UnconstrainedInfo.Top) => Changed(UnconstrainedInfo.Top)
    case (_, _) => Changed(UnconstrainedInfo.Top)
  }
