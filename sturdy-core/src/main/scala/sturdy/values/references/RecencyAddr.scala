package sturdy.values.references

import sturdy.data.given
import sturdy.effect.{ComputationJoiner, Effect, EffectListJoiner, TrySturdy}
import sturdy.values.*
import sturdy.values.references.{AbstractAddr, PowersetAddr, given}

import scala.collection.immutable.{BitSet, Map}
import scala.collection.mutable


enum Recency:
  case Recent
  case Old
  case Failed

given RencencyOrdering: Ordering[Recency] = Ordering.by[Recency, Int] {
  case Recency.Recent => 0
  case Recency.Old => 1
  case Recency.Failed => 2
}

enum PowRecency:
  case Recent
  case Old
  case RecentOld
  case Failed


given PowRencencyOrdering: Ordering[PowRecency] = Ordering.by[PowRecency, Int] {
  case PowRecency.Recent => 0
  case PowRecency.Old => 1
  case PowRecency.RecentOld => 2
  case PowRecency.Failed => 2
}

given CombinePowRencency[W <: Widening]: Combine[PowRecency, W] with
  override def apply(v1: PowRecency, v2: PowRecency): MaybeChanged[PowRecency] =
    (v1, v2) match
      case (PowRecency.Failed, _) => Unchanged(PowRecency.Failed)
      case (_, PowRecency.Failed) => Changed(PowRecency.Failed)
      case (PowRecency.Recent, PowRecency.Recent) => Unchanged(PowRecency.Recent)
      case (PowRecency.Old, PowRecency.Old) => Unchanged(PowRecency.Old)
      case (PowRecency.Recent, PowRecency.Old) | (PowRecency.Old, PowRecency.Recent) | (PowRecency.Recent, PowRecency.RecentOld) | (PowRecency.Old, PowRecency.RecentOld) => Changed(PowRecency.RecentOld)
      case (PowRecency.RecentOld, PowRecency.Recent) | (PowRecency.RecentOld, PowRecency.Old) | (PowRecency.RecentOld, PowRecency.RecentOld) => Unchanged(PowRecency.RecentOld)

case class RecencyRegion(recent: BitSet, old: BitSet, failed: BitSet):
  def recency: PowRecency =
    if(recent.isEmpty)
      PowRecency.Old
    else if(old.isEmpty)
      PowRecency.Recent
    else
      PowRecency.RecentOld

  def contains(n: Int): Boolean =
    recent.contains(n) || old.contains(n)

  def isEmpty: Boolean = recent.isEmpty && old.isEmpty

  def combineWithOlder(oldRegion: RecencyRegion): RecencyRegion =
    val joinedFailed = failed ++ oldRegion.failed
    RecencyRegion(
      recent = (recent ++ (oldRegion.recent -- old)) -- joinedFailed,
      old = (old ++ oldRegion.old) -- joinedFailed,
      failed = failed ++ joinedFailed
    )

  override def toString: String =
    s"[" +
      (if(recent.isEmpty) "" else s"recent: ${bitSetToRanges(recent).map(rangeToString).mkString(" ")}") +
      (if(old.isEmpty) "" else s", old: ${bitSetToRanges(old).map(rangeToString).mkString(" ")}") +
      (if(failed.isEmpty) "" else s", failed: ${bitSetToRanges(failed).map(rangeToString).mkString(" ")}") + "]"

  private def bitSetToRanges(bs: BitSet): List[Range] = {
    if (bs.isEmpty){
      Nil
    } else {
      val sorted = bs.toList.sorted
      val (ranges, lastStart, lastEnd) = sorted.tail.foldLeft((List.empty[Range], sorted.head, sorted.head)) {
        case ((acc, start, end), current) =>
          if (current == end + 1)
            (acc, start, current)
          else
            (Range(start, end + 1) :: acc, current, current)
      }
      (Range(lastStart, lastEnd + 1) :: ranges).reverse
    }
  }
  private def rangeToString(r: Range): String =
    if(r.size == 1)
      s"${r.start}"
    else
      s"[${r.start},${r.end-1}]"

given CombineRecencyRegion[W <: Widening]: Combine[RecencyRegion, W] =
  (region1: RecencyRegion, region2: RecencyRegion) =>
    val joinedFailed = region1.failed ++ region2.failed
    val joinedRecent = (region1.recent ++ region2.recent) -- joinedFailed
    val joinedOld = (region1.old ++ region2.old) -- joinedFailed
    val joinedRegion = RecencyRegion(joinedRecent, joinedOld, joinedFailed)
    MaybeChanged(joinedRegion, joinedRegion.recency != region1.recency)

final class AddressTranslation[Context](init: Map[Context, RecencyRegion]) extends Effect:
  var mapping: Map[Context,RecencyRegion] = init
  var otherMapping: Option[Map[Context,RecencyRegion]] = None
  var fresh: mutable.Map[Context,Int] = mutable.Map()

  inline def apply(ctx: Context, n: Int): PowPhysicalAddress[Context] = physicalAddresses(this.mapping, ctx, n)

  def physicalAddresses(mapping: Map[Context,RecencyRegion], ctx: Context, n: Int): PowPhysicalAddress[Context] =
    recency(mapping, ctx, n) match
      case PowRecency.RecentOld =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Recent), PhysicalAddress(ctx, Recency.Old))
      case PowRecency.Recent =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Recent))
      case PowRecency.Old =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Old))
      case PowRecency.Failed =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Failed))

  inline def recency(ctx: Context, n: Int): PowRecency =
    recency(mapping, ctx, n)

  def recency(mapping: Map[Context, RecencyRegion], ctx: Context, n: Int): PowRecency =
    mapping.get(ctx) match
      case Some(RecencyRegion(recent, old, failed)) =>
        if (failed.contains(n))
          assert(!recent.contains(n) && !old.contains(n), s"Virtual $n cannot be simultaneously failed and recent/old.")
          PowRecency.Failed
        else if (recent.contains(n) && old.contains(n))
          PowRecency.RecentOld
        else if (recent.contains(n))
          PowRecency.Recent
        else if (old.contains(n))
          PowRecency.Old
        else throw IllegalStateException(s"Virtual address ${ctx}@${n} is not bound to a physical address")
      case None => throw IllegalStateException(s"Virtual address ${ctx}@${n} is not bound to a physical address")

  def setRecency(virt: VirtualAddress[Context], powRecency: PowRecency) =
    val RecencyRegion(recent, old, failed) = region(mapping, virt.ctx).getOrElse(RecencyRegion(BitSet.empty, BitSet.empty, BitSet.empty))
      powRecency match
        case PowRecency.Recent =>
          setRegion(virt.ctx, RecencyRegion(recent + virt.n, old - virt.n, failed - virt.n))
        case PowRecency.Old =>
          setRegion(virt.ctx, RecencyRegion(recent - virt.n, old + virt.n, failed - virt.n))
        case PowRecency.RecentOld =>
          setRegion(virt.ctx, RecencyRegion(recent + virt.n, old + virt.n, failed - virt.n))
        case PowRecency.Failed =>
          setRegion(virt.ctx, RecencyRegion(recent - virt.n, old - virt.n, failed + virt.n))

  inline def region(ctx: Context): Option[RecencyRegion] =
    region(mapping, ctx)

  def region(mapping: Map[Context, RecencyRegion], ctx: Context): Option[RecencyRegion] =
    mapping.get(ctx)

  def setRegion(ctx: Context, region: RecencyRegion): Unit =
    mapping += ctx -> region

  def isEqual(v1: VirtualAddress[Context], v2: VirtualAddress[Context]): Boolean =
    v1.ctx == v2.ctx && {
      otherMapping match
        case Some(other) => recency(mapping, v1.ctx, v1.n) == recency(other, v2.ctx, v2.n)
        case None => recency(mapping, v1.ctx, v1.n) == recency(mapping, v2.ctx, v2.n)
    }

  def alloc(ctx: Context): VirtualAddress[Context] =
    val virt = freshVirt(ctx)

    mapping.get(ctx) match
      case Some(RecencyRegion(recent, old, failed)) =>
        mapping += ctx -> RecencyRegion(BitSet(virt.n), old ++ recent, failed)
      case None =>
        mapping += ctx -> RecencyRegion(BitSet(virt.n), BitSet.empty, BitSet.empty)

    virt

  inline def freshVirt(ctx: Context): VirtualAddress[Context] =
    val freshId = this.fresh.getOrElse(ctx, 0)
    this.fresh += (ctx) -> (freshId + 1)
    VirtualAddress(ctx, freshId, this)

  def allocNoRetire(ctx: Context, recency: PowRecency): VirtualAddress[Context] =
    val virt = freshVirt(ctx)
    setRecency(virt, recency)
    virt

  def joinRecentIntoOld(virt: VirtualAddress[Context]): Unit =
    for(region <- mapping.get(virt.ctx)) {
      mapping += virt.ctx -> RecencyRegion(region.recent - virt.n, region.old + virt.n, region.failed)
    }

  def deadPhysicalAddresses(alive: PowVirtualAddress[Context]): PowPhysicalAddress[Context] =
    var dead = mapping
    for((ctx,ns) <- alive.addrs.keys.groupMap(_._1)(_._2))
      dead.get(ctx) match
        case Some(region) =>
          if(ns.exists(region.contains))
            dead -= ctx
        case None => {}
    PowersetAddr(physicalAddressesByContext(dead).values.flatMap(_.addrs).toSet)

  def removePhysicalAddresses(physicalAddresses: PowPhysicalAddress[Context]): Unit =
    for(phys <- physicalAddresses.iterator)
      mapping.get(phys.ctx) match
        case Some(RecencyRegion(recent, old, failed)) =>
          phys.recency match
            case Recency.Recent =>
              if(old.isEmpty)
                mapping -= phys.ctx
              else
                mapping += phys.ctx -> RecencyRegion(BitSet.empty, old, failed)
            case Recency.Old =>
              if(recent.isEmpty)
                mapping -= phys.ctx
              else
                mapping += phys.ctx -> RecencyRegion(recent, BitSet.empty, failed)
            case Recency.Failed =>
              if(failed.isEmpty)
                mapping -= phys.ctx
              else
                mapping += phys.ctx -> RecencyRegion(recent, old, BitSet.empty)
        case None => {}

  override type State = AddressTranslationState

  override def getState: State = AddressTranslationState(mapping)

  override def setState(st: State): Unit =
    for((ctx, regionState) <- st.mapping) {
      mapping.get(ctx) match
        case None =>
          mapping += (ctx) -> regionState
        case Some(regionCurrent) if regionCurrent.combineWithOlder(regionState) != regionCurrent =>
          mapping += (ctx) -> regionCurrent.combineWithOlder(regionState)
        case Some(_) /* if regionCurrent.combineWithOlder(regionState) == regionCurrent */ => {}
    }

  override def setStateNonMonotonically(st: State): Unit =
    mapping = st.mapping

  override def setBottom: Unit = mapping = Map()

  given finiteVirt: Finite[Context] with {}
  override def join: Join[State] = (s1: State, s2: State) =>
    Join[Map[Context,RecencyRegion]](s1.mapping, s2.mapping).map(AddressTranslationState.apply)
  override def widen: Widen[State] = (s1: State, s2: State) =>
    Widen[Map[Context,RecencyRegion]](s1.mapping, s2.mapping).map(AddressTranslationState.apply)

  case class AddressTranslationState(mapping: Map[Context, RecencyRegion]):
    override def equals(obj: Any): Boolean =
      obj match
        case other: AddressTranslationState =>
          this.mapping.view.mapValues(_.recency).toMap.equals(other.mapping.view.mapValues(_.recency).toMap)
        case _ => false

    override def hashCode(): Int =
      this.mapping.view.mapValues(_.recency).toMap.hashCode()

    def difference(other: AddressTranslationState): AddressTranslationState =
      AddressTranslationState(
        mapping.flatMap {
          (ctx, region1) =>
            other.mapping.get(ctx) match
              case Some(region2) =>
                val newRegion = RecencyRegion(region1.recent.diff(region2.recent), region1.old.diff(region2.old), region1.failed.diff(region2.failed))
                if(newRegion.isEmpty)
                  None
                else
                  Some((ctx, newRegion))
              case None =>
                Some((ctx, region1))
        }
      )

    /** Computes the virtual addresses allocated between a starting and an end state and sets them to failed. */
    def failedVirts(before: AddressTranslationState): AddressTranslationState =
      AddressTranslationState(
        mapping.flatMap {
          (ctx, regionAfter) =>
            val regionBefore = before.mapping.getOrElse(ctx, RecencyRegion(BitSet.empty, BitSet.empty, BitSet.empty))
            val virtsAllocatedInFailedBranch = (regionAfter.recent ++ regionAfter.old).diff(regionBefore.recent ++ regionBefore.old)
            val newRegion = RecencyRegion(
              recent = BitSet.empty,
              old = BitSet.empty,
              failed = regionAfter.failed ++ regionBefore.failed ++ virtsAllocatedInFailedBranch
            )
            Some((ctx, newRegion))
        }
      )

    override def toString: String =
      s"AddressTranslationState(${hashCode()}, ${mapping.mkString(", ")})"

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A]:
    val before: State = AddressTranslationState(mapping)
    var afterFirst: State = _

    override def inbetween(fFailed: Boolean): Unit =
      afterFirst = AddressTranslationState(mapping)
      mapping = before.mapping

    override def retainNone(): Unit =
      val afterSecond = AddressTranslationState(mapping)
      mapping = join(before, join(afterFirst.failedVirts(before), afterSecond.failedVirts(before)).get).get.mapping

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      mapping = join(afterFirst, AddressTranslationState(mapping).failedVirts(before)).get.mapping

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      mapping = join(afterFirst.failedVirts(before), AddressTranslationState(mapping)).get.mapping

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      val afterSecond = AddressTranslationState(mapping)
      mapping = join(afterFirst, afterSecond).get.mapping
  )

  def virtualAddresses: PowVirtualAddress[Context] =
    PowVirtualAddress(mapping.flatMap((ctx,region) => (region.recent ++ region.old).view.map(n => VirtualAddress(ctx, n, this))).toList)

  def virtualAddresses(ctx: Context): PowVirtualAddress[Context] =
    mapping.get(ctx) match
      case Some(region) =>
        PowVirtualAddress((region.recent ++ region.old).view.map(n => VirtualAddress(ctx, n, this)))
      case None => PowVirtualAddress.empty

  inline def physicalAddressesByContext: Map[Context,PowPhysicalAddress[Context]] = physicalAddressesByContext(this.mapping)
  def physicalAddressesByContext(mapping: Map[Context,RecencyRegion]): Map[Context, PowPhysicalAddress[Context]] =
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

  override def toString: String = mapping.toString

object AddressTranslation:
  def empty[Context]: AddressTranslation[Context] = new AddressTranslation[Context](Map.empty)

case class VirtualAddress[Context](ctx: Context, n: Int, addressTrans: AddressTranslation[Context]) extends AbstractAddr[VirtualAddress[Context]]:

  if (toString == "r@map_15")
    println("break")

  def physical: PowPhysicalAddress[Context] =
    addressTrans(ctx, n)
  def recency: PowRecency =
    addressTrans.recency(ctx, n)
  override def isEmpty: Boolean = false
  override def isStrong: Boolean = physical.isStrong
  override def reduce[A](f: VirtualAddress[Context] => A)(using Join[A]): A = f(this)
  override def iterator: Iterator[VirtualAddress[Context]] = Iterator(this)
  override def toString: String = s"${ctx}_$n"

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
      joinedRecency.get match
        case PowRecency.Recent =>
          addrTrans.mapping += virt1.ctx -> RecencyRegion(region1.recent + virt1.n, region1.old - virt1.n, region1.failed - virt1.n)
        case PowRecency.Old =>
          addrTrans.mapping += virt1.ctx -> RecencyRegion(region1.recent - virt1.n, region1.old + virt1.n, region1.failed - virt1.n)
        case PowRecency.RecentOld =>
          addrTrans.mapping += virt1.ctx -> RecencyRegion(region1.recent + virt1.n, region1.old + virt1.n, region1.failed - virt1.n)
        case PowRecency.Failed =>
          addrTrans.mapping += virt1.ctx -> RecencyRegion(region1.recent - virt1.n, region1.old - virt1.n, region1.failed + virt1.n)
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



type PowPhysicalAddress[Context] = PowersetAddr[PhysicalAddress[Context], PhysicalAddress[Context]]

/**
 * Set of virtual addresses supporting mutating hashcodes.
 *
 * Invariant: `addrs` is empty iff `addressMap` is `None`.
 * Assumption: The address map of all virtual addresses is the same.
 */
class PowVirtualAddress[Context](val addrs: Map[(Context,Int), VirtualAddress[Context]]) extends AbstractAddr[VirtualAddress[Context]]:
  def virtualAddresses: Iterable[VirtualAddress[Context]] = addrs.values
  def physicalAddresses: Set[PhysicalAddress[Context]] = virtualAddresses.flatMap(_.physical.addrs).toSet
  def physicalAddresses(mapping: Map[Context,RecencyRegion]): Map[Context,PowRecency] =
    addressTranslation match
      case Some(addrTrans) =>
        addrs.keys.groupMapReduce(_._1)((ctx, idx) => addrTrans.recency(mapping, ctx, idx))(Join[PowRecency](_,_).get)
      case None => Map.empty

  override def isEmpty: Boolean = addrs.isEmpty

  def union(other: PowVirtualAddress[Context]): PowVirtualAddress[Context] =
    new PowVirtualAddress(this.addrs ++ other.addrs)

  override def isStrong: Boolean = virtualAddresses.forall(addr => addr.isStrong)

  override def reduce[A](f: VirtualAddress[Context] => A)(using Join[A]): A =
    virtualAddresses.map(f).reduce((a,b) => Join(a,b).get)

  override def iterator: Iterator[VirtualAddress[Context]] =
    virtualAddresses.iterator

  override def toString: String =
    s"PowVirtualAddresses(${addrs.keySet.map((ctx,n) => s"$ctx@$n").mkString(",")})"

  def addressTranslation: Option[AddressTranslation[Context]] =
    if(addrs.isEmpty)
      None
    else
      Some(addrs.head._2.addressTrans)

  override def equals(obj: Any): Boolean =
    obj match
      case other: PowVirtualAddress[Context] =>
        addressTranslation match
          case Some(addrMap) =>
            val mapping = addrMap.mapping
            val otherMapping = addrMap.otherMapping.getOrElse(mapping)
            val phys1 = this.physicalAddresses(mapping)
            val phys2 = other.physicalAddresses(otherMapping)
            phys1.equals(phys2)
          case None => other.isEmpty
      case _ => false

  override def hashCode(): Int =
    addressTranslation match
      case Some(addrTrans) =>
        physicalAddresses(addrTrans.mapping).hashCode()
      case None =>
        Map.empty.hashCode()

object PowVirtualAddress:
  def empty[Context]: PowVirtualAddress[Context] = new PowVirtualAddress[Context](Map.empty)
  def apply[Context](virtualAddresses: VirtualAddress[Context]*): PowVirtualAddress[Context] =
    apply(virtualAddresses)
  def apply[Context](virtualAddresses: IterableOnce[VirtualAddress[Context]]): PowVirtualAddress[Context] =
    new PowVirtualAddress(virtualAddresses.iterator.map(virt => ((virt.ctx,virt.n), virt)).toMap)

given CombinePowVirtualAddress[W <: Widening, Context]: Combine[PowVirtualAddress[Context], W] with
  override def apply(v1: PowVirtualAddress[Context], v2: PowVirtualAddress[Context]): MaybeChanged[PowVirtualAddress[Context]] =
    combinePowVirtualAddress(v1,v2)

def combinePowVirtualAddress[Context](v1: PowVirtualAddress[Context], v2: PowVirtualAddress[Context]): MaybeChanged[PowVirtualAddress[Context]] =
  v1.addressTranslation match
    case Some(addrTrans) =>
      val joined = new PowVirtualAddress(v1.addrs ++ v2.addrs)
      val phys1 = v1.physicalAddresses(addrTrans.mapping)
      val phys2 = v2.physicalAddresses(addrTrans.otherMapping.getOrElse(addrTrans.mapping))
      MaybeChanged(joined, Join(phys1,phys2).hasChanged)
    case None =>
      if(v2.isEmpty)
        Unchanged(v1)
      else
        Changed(v2)

given PowVirtAddrOrdering[Context]: PartialOrder[PowVirtualAddress[Context]] with
  override def lteq(x: PowVirtualAddress[Context], y: PowVirtualAddress[Context]): Boolean =
    x.physicalAddresses.subsetOf(y.physicalAddresses)
