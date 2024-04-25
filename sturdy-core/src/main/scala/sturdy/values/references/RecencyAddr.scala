package sturdy.values.references

import sturdy.data.given
import sturdy.effect.{ComputationJoiner, Effect, EffectListJoiner, TrySturdy}
import sturdy.values.*
import sturdy.values.references.{AbstractAddr, PowersetAddr, given}

import scala.collection.immutable.{BitSet, Map}
import scala.collection.mutable

case class RecencyRegion(recent: BitSet = BitSet.empty, old: BitSet = BitSet.empty):
  def recency: PowRecency =
    if(recent.isEmpty)
      PowRecency.Old
    else if(old.isEmpty)
      PowRecency.Recent
    else
      PowRecency.RecentOld

given CombineRecencyRegion[W <: Widening]: Combine[RecencyRegion, W] =
  (region1: RecencyRegion, region2: RecencyRegion) =>
    val newRegion = RecencyRegion(region1.recent ++ region2.recent, region1.old ++ region2.old)
    MaybeChanged(newRegion, newRegion.recency != region1.recency)

final class AddressTranslation[Context](init: Map[Context, RecencyRegion]) extends Effect:
  var mapping: Map[Context,RecencyRegion] = init
  var otherMapping: Option[Map[Context,RecencyRegion]] = None
  var fresh: mutable.Map[Context,Int] = mutable.Map()

  def apply(ctx: Context, n: Int): PowPhysicalAddress[Context] =
    recency(mapping, ctx, n) match
      case PowRecency.RecentOld =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Recent), PhysicalAddress(ctx, Recency.Old))
      case PowRecency.Recent =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Recent))
      case PowRecency.Old =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Old))

  def recency(ctx: Context, n: Int): PowRecency =
    recency(mapping, ctx, n)

  def recency(mapping: Map[Context, RecencyRegion], ctx: Context, n: Int): PowRecency =
    mapping.get(ctx) match
      case Some(RecencyRegion(recent, old)) =>
        if (recent.contains(n) && old.contains(n))
          PowRecency.RecentOld
        else if (recent.contains(n))
          PowRecency.Recent
        else if (old.contains(n))
          PowRecency.Old
        else throw IllegalStateException(s"Virtual address ${ctx}@${n} is not bound to a physical address")
      case None => throw IllegalStateException(s"Virtual address ${ctx}@${n} is not bound to a physical address")

  def region(ctx: Context): Option[RecencyRegion] =
    region(mapping, ctx)

  def region(mapping: Map[Context, RecencyRegion], ctx: Context): Option[RecencyRegion] =
    mapping.get(ctx)

  def isEqual(v1: VirtualAddress[Context], v2: VirtualAddress[Context]): Boolean =
    v1.ctx == v2.ctx && {
      otherMapping match
        case Some(other) => recency(mapping, v1.ctx, v1.n) == recency(other, v2.ctx, v2.n)
        case None => recency(mapping, v1.ctx, v1.n) == recency(mapping, v2.ctx, v2.n)
    }

  def alloc(ctx: Context): VirtualAddress[Context] =
    val freshId = this.fresh.getOrElse(ctx, 0)
    this.fresh += (ctx) -> (freshId + 1)

    mapping.get(ctx) match
      case Some(RecencyRegion(recent, old)) =>
        mapping += ctx -> RecencyRegion(BitSet(freshId), old ++ recent)
      case None =>
        mapping += ctx -> RecencyRegion(BitSet(freshId), BitSet())

    VirtualAddress(ctx, freshId, this)

  def allocNoRetire(ctx: Context): VirtualAddress[Context] =
    val freshId = this.fresh.getOrElse(ctx, 0)
    this.fresh += (ctx) -> (freshId + 1)

    mapping.get(ctx) match
      case Some(RecencyRegion(recent, old)) =>
        mapping += ctx -> RecencyRegion(recent + freshId, old)
      case None =>
        mapping += ctx -> RecencyRegion(BitSet(freshId), BitSet())

    VirtualAddress(ctx, freshId, this)

  def joinRecentIntoOld(virt: VirtualAddress[Context]) =
    for(region <- mapping.get(virt.ctx)) {
      mapping += virt.ctx -> RecencyRegion(region.recent - virt.n, region.old + virt.n)
    }

  override type State = Map[Context,RecencyRegion]

  override def getState: State =
    mapping

  override def setState(st: State): Unit =
    for((ctx, regionState) <- st) {
      mapping.get(ctx) match
        case None =>
          mapping += (ctx) -> regionState
        case Some(regionCurrent) =>
          mapping += (ctx) -> Join(regionCurrent, regionState).get
    }

  given finiteVirt: Finite[Context] with {}
  override def join: Join[State] = implicitly[Join[State]]
  override def widen: Widen[State] = implicitly[Widen[State]]

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A]:
    val snapshotMapping = mapping
    private var afterFirst: State = _

    override def inbetween(): Unit =
      afterFirst = mapping
      mapping = snapshotMapping

    override def retainNone(): Unit =
      mapping = snapshotMapping

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      mapping = afterFirst

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      () // do nothing

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      val afterSecond = mapping
      val joined = join(afterFirst, afterSecond)
      mapping = joined.get
  )

  def virtualAddresses: PowVirtualAddress[Context] =
    PowVirtualAddress(mapping.flatMap((ctx,region) => (region.recent ++ region.old).view.map(n => VirtualAddress(ctx, n, this))).toList)

  def virtualAddresses(ctx: Context): PowVirtualAddress[Context] =
    mapping.get(ctx) match
      case Some(region) =>
        PowVirtualAddress((region.recent ++ region.old).view.map(n => VirtualAddress(ctx, n, this)))
      case None => PowVirtualAddress.empty

  def physicalAddressesByContext: Map[Context, PowPhysicalAddress[Context]] =
    mapping.map((ctx,region) =>
      if(region.recent.isEmpty)
        (ctx, PowersetAddr(PhysicalAddress(ctx, Recency.Old)))
      else if(region.old.isEmpty)
        (ctx, PowersetAddr(PhysicalAddress(ctx, Recency.Recent)))
      else
        (ctx, PowersetAddr(PhysicalAddress(ctx, Recency.Recent), PhysicalAddress(ctx, Recency.Old)))
    )

  def virtualAddressesByContext: Map[Context, PowVirtualAddress[Context]] =
    mapping.map((ctx,region) =>
      (ctx, PowVirtualAddress((region.recent ++ region.old).view.map(n => VirtualAddress(ctx,n,this))))
    )

  override def clone(): AddressTranslation[Context] = new AddressTranslation[Context](mapping)

object AddressTranslation:
  def empty[Context]: AddressTranslation[Context] = new AddressTranslation[Context](Map.empty)

case class VirtualAddress[Context](ctx: Context, n: Int, addressTrans: AddressTranslation[Context]) extends AbstractAddr[VirtualAddress[Context]]:
  def physical: PowPhysicalAddress[Context] =
    addressTrans(ctx, n)
  def recency: PowRecency =
    addressTrans.recency(ctx, n)
  override def isEmpty: Boolean = false
  override def isStrong: Boolean = physical.isStrong
  override def reduce[A](f: VirtualAddress[Context] => A)(using Join[A]): A = f(this)
  override def iterator: Iterator[VirtualAddress[Context]] = Iterator(this)
  override def toString: String = s"VirtualAddr($ctx, $n)"

  final override def equals(obj: Any): Boolean =
    obj match
      case other: VirtualAddress[Context @unchecked] =>
        addressTrans.isEqual(this, other)
      case _ => false

  final override def hashCode(): Int =
    physical.hashCode()

  final def identifier: (Context,Int) = (ctx,n)

  final def addressTranslation: AddressTranslation[Context] = addressTrans

given VirtualAddressOrdering[Context : Ordering]: Ordering[VirtualAddress[Context]] =
  Ordering.by(virt => virt.identifier)

given VirtualAddressJoin[Context]: Join[VirtualAddress[Context]] =
  (virt1: VirtualAddress[Context], virt2: VirtualAddress[Context]) =>
    if(virt1.ctx == virt2.ctx) {
      val addrTrans = virt1.addressTrans
      val mapping = addrTrans.mapping
      val otherMapping = addrTrans.otherMapping.getOrElse(mapping)
      val region1 = addrTrans.region(mapping, virt1.ctx).get
      val recency1 = addrTrans.recency(mapping, virt1.ctx, virt1.n)
      val recency2 = addrTrans.recency(otherMapping, virt2.ctx, virt2.n)
      val joinedRecency = Join(recency1, recency2)
      if (joinedRecency.hasChanged)
        addrTrans.mapping += virt1.ctx -> RecencyRegion(region1.recent + virt1.n, region1.old + virt1.n)
      joinedRecency.map(_ => virt1)
    } else {
      throw new IllegalArgumentException(s"Cannot join virtual addresses with different contexts $virt1, $virt2")
    }




case class PhysicalAddress[Context](ctx: Context, recency: Recency) extends AbstractAddr[PhysicalAddress[Context]]:
  override def isEmpty: Boolean = false
  override def isStrong: Boolean = recency == Recency.Recent
  override def reduce[A](f: PhysicalAddress[Context] => A)(using Join[A]): A = f(this)
  override def iterator: Iterator[PhysicalAddress[Context]] = Iterator(this)
  override def toString() = s"${ctx}_${recency}"

given PhysicalAddressOrdering[Context: Ordering]: Ordering[PhysicalAddress[Context]] =
  Ordering.by(addr => (addr.ctx, addr.recency))

given finitePhysicalAddr[Context](using Finite[Context]): Finite[PhysicalAddress[Context]] with {}

given finiteVirtualAddr[Context](using Finite[PhysicalAddress[Context]]): Finite[VirtualAddress[Context]] with {}
given structuralVirtualAddr[Context]: Structural[VirtualAddress[Context]] with {}

enum Recency:
  case Recent
  case Old

given RencencyOrdering: Ordering[Recency] = Ordering.by[Recency, Int] {
  case Recency.Recent => 0
  case Recency.Old => 1
}

enum PowRecency:
  case Recent
  case Old
  case RecentOld


given PowRencencyOrdering: Ordering[PowRecency] = Ordering.by[PowRecency, Int] {
  case PowRecency.Recent => 0
  case PowRecency.Old => 1
  case PowRecency.RecentOld => 2
}

given CombinePowRencency[W <: Widening]: Combine[PowRecency, W] with
  override def apply(v1: PowRecency, v2: PowRecency): MaybeChanged[PowRecency] =
    (v1,v2) match
      case (PowRecency.Recent, PowRecency.Recent) => Unchanged(PowRecency.Recent)
      case (PowRecency.Old, PowRecency.Old) => Unchanged(PowRecency.Old)
      case (PowRecency.RecentOld, _) => Unchanged(PowRecency.RecentOld)
      case (_,_) => Changed(PowRecency.RecentOld)


type PowPhysicalAddress[Context] = PowersetAddr[PhysicalAddress[Context], PhysicalAddress[Context]]

/**
 * Set of virtual addresses supporting mutating hashcodes.
 *
 * Invariant: `addrs` is empty iff `addressMap` is `None`.
 * Assumption: The address map of all virtual addresses is the same.
 */
class PowVirtualAddress[Context](val addrs: Map[Context, BitSet], val addressMap: Option[AddressTranslation[Context]]) extends AbstractAddr[VirtualAddress[Context]]:
  def virtualAddresses: Iterable[VirtualAddress[Context]] =
    addressMap match
      case Some(addrMap) =>  addrs.flatMap((ctx,idxs) => idxs.view.map(idx => VirtualAddress(ctx, idx, addrMap)))
      case None => Iterable.empty

  def physicalAddresses: Set[PhysicalAddress[Context]] = virtualAddresses.flatMap(_.physical.addrs).toSet

  override def isEmpty: Boolean = addrs.isEmpty

  override def isStrong: Boolean = virtualAddresses.forall(addr => addr.isStrong)

  override def reduce[A](f: VirtualAddress[Context] => A)(using Join[A]): A =
    virtualAddresses.map(f).reduce((a,b) => Join(a,b).get)

  override def iterator: Iterator[VirtualAddress[Context]] =
    virtualAddresses.iterator

  override def toString: String =
    s"PowVirtualAddresses(${addrs})"

  override def equals(obj: Any): Boolean =
    obj match
      case other: PowVirtualAddress[?] =>
        addressMap match
          case Some(addrMap) =>
            val mapping = addrMap.mapping
            val otherMapping = addrMap.otherMapping.getOrElse(mapping)
            val phys1 = addrs.map((ctx, idxs) => (ctx, idxs.map(idx => addrMap.recency(mapping, ctx, idx))))
            val phys2 = other.addrs.map((ctx, idxs) => (ctx, idxs.map(idx => addrMap.recency(otherMapping, ctx.asInstanceOf, idx))))
            phys1.equals(phys2)
          case None => other.addressMap.isEmpty
      case _ => false

  override def hashCode(): Int =
    addressMap match
      case Some(addrMap) =>
        addrs.map((ctx, idxs) => (ctx, idxs.map(idx => addrMap.recency(ctx,idx)))).hashCode
      case None => Map().hashCode()

object PowVirtualAddress:
  def empty[Context]: PowVirtualAddress[Context] = new PowVirtualAddress[Context](Map.empty, None)
  def apply[Context](virtualAddresses: VirtualAddress[Context]*): PowVirtualAddress[Context] =
    apply(virtualAddresses)
  def apply[Context](virtualAddresses: Iterable[VirtualAddress[Context]]): PowVirtualAddress[Context] =
    var addrs = Map.empty[Context, BitSet]
    var addrMap: Option[AddressTranslation[Context]] = None
    for (virt <- virtualAddresses) {
      val (ctx, n) = virt.identifier
      addrs += ctx -> (addrs.getOrElse(ctx, BitSet.empty) + n)
      addrMap = Some(virt.addressTranslation)
    }
    new PowVirtualAddress(addrs, addrMap)

given CombinePowVirtualAddress[W <: Widening, Context]: Combine[PowVirtualAddress[Context], W] with
  override def apply(v1: PowVirtualAddress[Context], v2: PowVirtualAddress[Context]): MaybeChanged[PowVirtualAddress[Context]] =
    combinePowVirtualAddress(v1,v2)

def combinePowVirtualAddress[Context](v1: PowVirtualAddress[Context], v2: PowVirtualAddress[Context]): MaybeChanged[PowVirtualAddress[Context]] =
    (v1.addressMap,v2.addressMap) match
      case (None, None) => Unchanged(v1)
      case (Some(_), None) => Changed(v1)
      case (None, Some(_)) => Changed(v2)
      case (Some(addrTrans), Some(_)) =>
        var joined = v1.addrs
        var changed = false
        for ((ctx2, indices2) <- v2.addrs)
          joined.get(ctx2) match
            case None =>
              joined += ctx2 -> indices2
              changed = true
            case Some(indices1) =>
              if(! changed) {
                val physicals1 = indices1.map(idx => addrTrans.recency(addrTrans.mapping, ctx2, idx)).reduce(Join(_,_).get)
                val physicals2 = indices2.map(idx => addrTrans.recency(addrTrans.otherMapping.getOrElse(addrTrans.mapping), ctx2, idx)).reduce(Join(_,_).get)
                val joinedPhysicals = Join(physicals1,physicals2)
                changed ||= joinedPhysicals.hasChanged
              }
              joined += ctx2 -> indices1.union(indices2)
        val res = MaybeChanged(new PowVirtualAddress(joined, v1.addressMap), changed)
        res

given PowVirtAddrOrdering[Context]: PartialOrder[PowVirtualAddress[Context]] with
  override def lteq(x: PowVirtualAddress[Context], y: PowVirtualAddress[Context]): Boolean =
    x.physicalAddresses.subsetOf(y.physicalAddresses)
