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


given PowRecencyOrdering: Ordering[PowRecency] = Ordering.by[PowRecency, Int] {
  case PowRecency.Recent => 0
  case PowRecency.Old => 1
  case PowRecency.RecentOld => 2
  case PowRecency.Failed => 2
}

given CombinePowRecency[W <: Widening]: Combine[PowRecency, W] with
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

trait AddressTranslation[Context: Finite] extends Effect:
  var fresh: mutable.Map[Context,Int] = mutable.Map()

  protected def stateHasAddrTransState: HasAddressTranslationState[Context,State]
  given HasAddressTranslationState[Context,State] = stateHasAddrTransState

  def nullState: State

  def internalState: State =
    internalStateOption.getOrElse(throw Error("AddressTranslation.internalState is null"))
  def internalStateOption: Option[State]
  def withInternalState[A](f: State => (A, State)): A
  def modifyInternalState(f: State => State): Unit
  def setInternalState(state: State): Unit

  def leftState: Option[State]
  def withLeftState[A](f: State => (A, State)): A
  def setLeftState(state: State): Unit

  def rightState: Option[State]
  def withRightState[A](f: State => (A, State)): A
  def setRightState(state: State): Unit

  inline def internalAddressTranslationState: AddressTranslationState[Context] =
    AddressTranslationState[Context,State].get(internalState)
  def leftAddressTranslationState: AddressTranslationState[Context] =
    AddressTranslationState[Context,State].get(leftState.getOrElse(internalState))
  inline def rightAddressTranslationState: AddressTranslationState[Context] =
    AddressTranslationState[Context,State].get(rightState.getOrElse(internalState))
  inline def modifyInternalAddressTranslationState(f: AddressTranslationState[Context] => AddressTranslationState[Context]): Unit =
    modifyInternalState(state => AddressTranslationState[Context,State].modify(state)(f))

  inline def apply(ctx: Context, n: Int): PowPhysicalAddress[Context] = physicalAddresses(ctx, n, internalState)

  def physicalAddresses[State](using HasAddressTranslationState[Context,State])(ctx: Context, n: Int, state: State): PowPhysicalAddress[Context] =
    recency(ctx, n, state) match
      case PowRecency.RecentOld =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Recent), PhysicalAddress(ctx, Recency.Old))
      case PowRecency.Recent =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Recent))
      case PowRecency.Old =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Old))
      case PowRecency.Failed =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Failed))

  inline def recency(ctx: Context, n: Int): PowRecency =
    recency(ctx, n, internalState)

  def recency[State](using HasAddressTranslationState[Context,State])(ctx: Context, n: Int, state: State): PowRecency =
    AddressTranslationState[Context,State].get(state).mapping.get(ctx) match
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
        else throw Error(s"Virtual address ${ctx}@${n} is not bound to a physical address")
      case None => throw Error(s"Virtual address ${ctx}@${n} is not bound to a physical address")

  inline def setRecency(virt: VirtualAddress[Context], powRecency: PowRecency): Unit =
    modifyInternalState(setRecencyPure(virt, powRecency, _))

  def setRecencyPure[State](using HasAddressTranslationState[Context,State])(virt: VirtualAddress[Context], powRecency: PowRecency, state: State): State =
    val RecencyRegion(recent, old, failed) = regionPure(virt.ctx, state).getOrElse(RecencyRegion(BitSet.empty, BitSet.empty, BitSet.empty))
      powRecency match
        case PowRecency.Recent =>
          setRegion(virt.ctx, RecencyRegion(recent + virt.n, old - virt.n, failed - virt.n), state)
        case PowRecency.Old =>
          setRegion(virt.ctx, RecencyRegion(recent - virt.n, old + virt.n, failed - virt.n), state)
        case PowRecency.RecentOld =>
          setRegion(virt.ctx, RecencyRegion(recent + virt.n, old + virt.n, failed - virt.n), state)
        case PowRecency.Failed =>
          setRegion(virt.ctx, RecencyRegion(recent - virt.n, old - virt.n, failed + virt.n), state)

  inline def region(ctx: Context): Option[RecencyRegion] =
    regionPure(ctx, internalState)

  inline def regionPure[State](using HasAddressTranslationState[Context,State])(ctx: Context, state: State): Option[RecencyRegion] =
    AddressTranslationState[Context,State].get(state).mapping.get(ctx)

  inline def setRegion[State](using HasAddressTranslationState[Context,State])(ctx: Context, region: RecencyRegion, state:State): State =
    AddressTranslationState[Context,State].modify(state)(addrTransState => AddressTranslationState(addrTransState.mapping + (ctx -> region)))

  inline def isEqual(v1: VirtualAddress[Context], v2: VirtualAddress[Context]): Boolean =
    v1.ctx == v2.ctx && recency(v1.ctx, v1.n, leftState.getOrElse(internalState)) == recency(v2.ctx, v2.n, rightState.getOrElse(internalState))

  inline def alloc(ctx: Context): VirtualAddress[Context] =
    withInternalState(allocPure(ctx,_))

  def allocPure[State](using HasAddressTranslationState[Context,State])(ctx: Context, state: State): (VirtualAddress[Context],State) =
    AddressTranslationState[Context,State]._with(state){ case AddressTranslationState(mapping) =>
      val virt = freshVirt(ctx)
      mapping.get(ctx) match
        case Some(RecencyRegion(recent, old, failed)) =>
          (virt, AddressTranslationState(mapping + (ctx -> RecencyRegion(BitSet(virt.n), old ++ recent, failed))))
        case None =>
          (virt, AddressTranslationState(mapping + (ctx -> RecencyRegion(BitSet(virt.n), BitSet.empty, BitSet.empty))))
    }

  inline def freshVirt(ctx: Context): VirtualAddress[Context] =
    val freshId = this.fresh.getOrElse(ctx, 0)
    this.fresh += (ctx) -> (freshId + 1)
    VirtualAddress(ctx, freshId, this)

  def allocNoRetire[State](using HasAddressTranslationState[Context,State])(ctx: Context, recency: PowRecency, state: State): (VirtualAddress[Context],State) =
    val virt = freshVirt(ctx)
    val state1 = setRecencyPure(virt, recency,state)
    (virt,state1)

  def joinRecentIntoOld[State](using HasAddressTranslationState[Context,State])(virt: VirtualAddress[Context], state: State): State =
    AddressTranslationState[Context,State].modify(state) { case AddressTranslationState(mapping) =>
      AddressTranslationState(
        mapping.get(virt.ctx).map(region =>
          mapping + (virt.ctx -> RecencyRegion(region.recent - virt.n, region.old + virt.n, region.failed))
        ).getOrElse(mapping)
      )
    }

  def deadPhysicalAddresses[State](using HasAddressTranslationState[Context,State])(alive: PowVirtualAddress[Context], state: State = internalState): PowPhysicalAddress[Context] =
    var dead = AddressTranslationState[Context,State].get(state).mapping
    for((ctx,ns) <- alive.addrs.keys.groupMap(_._1)(_._2))
      dead.get(ctx) match
        case Some(region) =>
          if(ns.exists(region.contains))
            dead -= ctx
        case None => {}
    PowersetAddr(physicalAddressesByContext(AddressTranslationState(dead)).values.flatMap(_.addrs).toSet)

  def removePhysicalAddresses[State](using HasAddressTranslationState[Context,State])(physicalAddresses: PowPhysicalAddress[Context], state: State): State =
    AddressTranslationState[Context,State].modify(state) { addrTransState =>
      var mapping = addrTransState.mapping
      for (phys <- physicalAddresses.iterator)
        mapping.get(phys.ctx) match
          case Some(RecencyRegion(recent, old, failed)) =>
            phys.recency match
              case Recency.Recent =>
                if (old.isEmpty)
                  mapping -= phys.ctx
                else
                  mapping += phys.ctx -> RecencyRegion(BitSet.empty, old, failed)
              case Recency.Old =>
                if (recent.isEmpty)
                  mapping -= phys.ctx
                else
                  mapping += phys.ctx -> RecencyRegion(recent, BitSet.empty, failed)
              case Recency.Failed =>
                if (failed.isEmpty)
                  mapping -= phys.ctx
                else
                  mapping += phys.ctx -> RecencyRegion(recent, old, BitSet.empty)
          case None => {}

      AddressTranslationState(mapping)
    }

  def virtualAddresses: PowVirtualAddress[Context] =
    PowVirtualAddress(internalAddressTranslationState.mapping.flatMap((ctx,region) => (region.recent ++ region.old).view.map(n => VirtualAddress(ctx, n, this))).toList)

  def virtualAddresses(ctx: Context): PowVirtualAddress[Context] =
    internalAddressTranslationState.mapping.get(ctx) match
      case Some(region) =>
        PowVirtualAddress((region.recent ++ region.old).view.map(n => VirtualAddress(ctx, n, this)))
      case None => PowVirtualAddress.empty

  inline def physicalAddressesByContext: Map[Context,PowPhysicalAddress[Context]] = physicalAddressesByContext(internalAddressTranslationState)
  def physicalAddressesByContext(state: AddressTranslationState[Context]): Map[Context, PowPhysicalAddress[Context]] =
    state.mapping.map((ctx,region) =>
      if(region.recent.isEmpty)
        (ctx, PowersetAddr(PhysicalAddress(ctx, Recency.Old)))
      else if(region.old.isEmpty)
        (ctx, PowersetAddr(PhysicalAddress(ctx, Recency.Recent)))
      else
        (ctx, PowersetAddr(PhysicalAddress(ctx, Recency.Recent), PhysicalAddress(ctx, Recency.Old)))
    )

  def virtualAddressesByContext: Map[Context, PowVirtualAddress[Context]] =
    internalAddressTranslationState.mapping.map((ctx,region) =>
      (ctx, PowVirtualAddress((region.recent ++ region.old).view.map(n => VirtualAddress(ctx,n,this))))
    )

  override def clone(): AddressTranslation[Context] = throw UnsupportedOperationException()


case class AddressTranslationState[Context](mapping: Map[Context, RecencyRegion]):
  override def equals(obj: Any): Boolean =
    obj match
      case other: AddressTranslationState[Context]@unchecked =>
        this.mapping.view.mapValues(_.recency).toMap.equals(other.mapping.view.mapValues(_.recency).toMap)
      case _ => false

  override def hashCode(): Int =
    this.mapping.view.mapValues(_.recency).toMap.hashCode()

  def difference(other: AddressTranslationState[Context]): AddressTranslationState[Context] =
    AddressTranslationState(
      mapping.flatMap {
        (ctx, region1) =>
          other.mapping.get(ctx) match
            case Some(region2) =>
              val newRegion = RecencyRegion(region1.recent.diff(region2.recent), region1.old.diff(region2.old), region1.failed.diff(region2.failed))
              if (newRegion.isEmpty)
                None
              else
                Some((ctx, newRegion))
            case None =>
              Some((ctx, region1))
      }
    )

  /** Computes the virtual addresses allocated between a starting and an end state and sets them to failed. */
  def failedVirts(before: AddressTranslationState[Context]): AddressTranslationState[Context] =
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

given AddressTranslationStateCombine[Ctx: Finite, W <: Widening]: Combine[AddressTranslationState[Ctx], W] =
  (s1: AddressTranslationState[Ctx], s2: AddressTranslationState[Ctx]) =>
    Combine[Map[Ctx, RecencyRegion], W](s1.mapping, s2.mapping).map(AddressTranslationState.apply)


trait HasAddressTranslationState[Context, State]:
  def get(state: State): AddressTranslationState[Context] = _with(state)(st => (st, st))._1
  inline def set(state: State, addressTranslationState: AddressTranslationState[Context]): State = modify(state)(_ => addressTranslationState)
  def _with[A](state: State)(f: AddressTranslationState[Context] => (A, AddressTranslationState[Context])): (A, State)
  inline def modify(state: State)(f: AddressTranslationState[Context] => AddressTranslationState[Context]): State = _with(state)(st => ((), f(st)))._2

object AddressTranslationState:
  def apply[Context, State](using hasAddrTransState: HasAddressTranslationState[Context, State]): HasAddressTranslationState[Context, State] = hasAddrTransState

given HasAddressTranslationStateId[Context]: HasAddressTranslationState[Context, AddressTranslationState[Context]] with
  override inline def _with[A](state: AddressTranslationState[Context])(f: AddressTranslationState[Context] => (A, AddressTranslationState[Context])): (A, AddressTranslationState[Context]) =
    f(state)

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
      import addrTrans.given
      val region1 = addrTrans.regionPure(virt1.ctx, addrTrans.leftState.getOrElse(addrTrans.internalState)).get
      val recency1 = addrTrans.recency(virt1.ctx, virt1.n, addrTrans.leftState.getOrElse(addrTrans.internalState))
      val recency2 = addrTrans.recency(virt2.ctx, virt2.n, addrTrans.rightState.getOrElse(addrTrans.internalState))
      val joinedRecency = Join(recency1, recency2)
      addrTrans.setRecency(virt1, joinedRecency.get)
      joinedRecency.map(_ => virt1)
    } else {
      throw new CannotJoinException(s"Cannot join virtual addresses with different contexts $virt1, $virt2")
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
  def physicalAddresses(state: AddressTranslationState[Context]): Map[Context,PowRecency] =
    addressTranslation match
      case Some(addrTrans) =>
        addrs.keys.groupMapReduce(_._1)((ctx, idx) => addrTrans.recency(ctx, idx, state))(Join[PowRecency](_,_).get)
      case None => Map.empty

  override def isEmpty: Boolean = addrs.isEmpty

  def add(newAddrs: VirtualAddress[Context]*): PowVirtualAddress[Context] =
    this.union(PowVirtualAddress(newAddrs))

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
      case other: PowVirtualAddress[Context] @unchecked =>
        addressTranslation match
          case Some(addrMap) =>
            val phys1 = this.physicalAddresses(addrMap.leftAddressTranslationState)
            val phys2 = other.physicalAddresses(addrMap.rightAddressTranslationState)
            phys1.equals(phys2)
          case None => other.isEmpty
      case _ => false

  override def hashCode(): Int =
    addressTranslation match
      case Some(addrTrans) =>
        physicalAddresses(addrTrans.internalAddressTranslationState).hashCode()
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

def combinePowVirtualAddress[W <: Widening, Context](v1: PowVirtualAddress[Context], v2: PowVirtualAddress[Context]): MaybeChanged[PowVirtualAddress[Context]] =
  v1.addressTranslation match
    case Some(addrTrans) =>
      val joined = new PowVirtualAddress(v1.addrs ++ v2.addrs)
      val phys1 = v1.physicalAddresses(addrTrans.leftAddressTranslationState)
      val phys2 = v2.physicalAddresses(addrTrans.rightAddressTranslationState)
      val changed = Join(phys1,phys2).hasChanged
      MaybeChanged(joined, changed)
    case None =>
      if(v2.isEmpty)
        Unchanged(v1)
      else
        Changed(v2)

given PowVirtAddrOrdering[Context]: PartialOrder[PowVirtualAddress[Context]] with
  override def lteq(x: PowVirtualAddress[Context], y: PowVirtualAddress[Context]): Boolean =
    x.physicalAddresses.subsetOf(y.physicalAddresses)
