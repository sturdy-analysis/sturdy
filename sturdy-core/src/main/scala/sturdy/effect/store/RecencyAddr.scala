package sturdy.effect.store

import sturdy.values.{Changed, Combine, Finite, MaybeChanged, Unchanged, Widening}

class VirtualAddress[Context](val ctx: Context, val n: Int,
                              addressTranslation: (Context,Int) => Iterable[PhysicalAddress[Context]]):

  final override def equals(obj: Any): Boolean =
    obj match
      case other: VirtualAddress[?] => this.lookupPhysicalAddress == other.lookupPhysicalAddress
      case _ => false

  final override def hashCode(): Int =
    lookupPhysicalAddress.hashCode()

  final def identifier: (Context,Int) = (ctx,n)

  final def lookupPhysicalAddress: Set[PhysicalAddress[Context]] =
    addressTranslation(ctx,n).toSet

case class PhysicalAddress[Context](ctx: Context, recency: Recency) extends
  ManageableAddr(false)

given finitePhysicalAddr[Context]: Finite[PhysicalAddress[Context]] with {}

enum Recency:
  case Recent
  case Old

enum PowRecency:
  case Recent
  case Old
  case RecentOld

given CombinePowRencency[W <: Widening]: Combine[PowRecency, W] with
  override def apply(v1: PowRecency, v2: PowRecency): MaybeChanged[PowRecency] =
    (v1,v2) match
      case (PowRecency.Recent, PowRecency.Recent) => Unchanged(PowRecency.Recent)
      case (PowRecency.Old, PowRecency.Old) => Unchanged(PowRecency.Old)
      case (PowRecency.RecentOld, PowRecency.RecentOld) => Unchanged(PowRecency.RecentOld)
      case (_,_) => Changed(PowRecency.RecentOld)
