package sturdy.values.references

import sturdy.data.given
import sturdy.effect.Effect
import sturdy.values.*
import sturdy.values.references.{AbstractAddr, PowersetAddr, given}

import scala.collection.immutable.{BitSet, Map}

case class RecencyRegion(recent: BitSet, old: BitSet):
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
  var fresh: Int = 0

  def apply(ctx: Context, n: Int): PowPhysicalAddress[Context] =
    recency(mapping, ctx, n) match
      case PowRecency.RecentOld =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Recent), PhysicalAddress(ctx, Recency.Old))
      case PowRecency.Recent =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Recent))
      case PowRecency.Old =>
        PowersetAddr(PhysicalAddress(ctx, Recency.Old))

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

  def isEqual(v1: VirtualAddress[Context], v2: VirtualAddress[Context]): Boolean =
    v1.ctx == v2.ctx && {
      otherMapping match
        case Some(other) => recency(mapping, v1.ctx, v1.n) == recency(other, v2.ctx, v2.n)
        case None => recency(mapping, v1.ctx, v1.n) == recency(mapping, v2.ctx, v2.n)
    }

  def alloc(ctx: Context): VirtualAddress[Context] =
    val freshId = this.fresh
    this.fresh += 1

    mapping.get(ctx) match
      case Some(RecencyRegion(recent, old)) =>
        mapping += ctx -> RecencyRegion(BitSet(freshId), old ++ recent)
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
    mapping = st

  override def mapState(st: State, f: [A] => A => A): State = st

  given finiteVirt: Finite[Context] with {}
  override def join: Join[State] = implicitly[Join[State]]
  override def widen: Widen[State] = implicitly[Widen[State]]

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

final class AddressClosure[Context](val addrTrans: AddressTranslation[Context], val effect: Effect) extends Effect:

  case class AddressClosureState(addrTransState: addrTrans.State, effectState: effect.State):
    override def equals(obj: Any): Boolean =
      obj match
        case other: AddressClosureState =>
          val snapshotMapping = addrTrans.mapping
          val snapshotOtherMapping = addrTrans.otherMapping
          try {
            addrTrans.mapping = this.addrTransState
            addrTrans.otherMapping = Some(other.addrTransState)
            this.effectState.equals(other.effectState)
          } finally {
            addrTrans.mapping = snapshotMapping
            addrTrans.otherMapping = snapshotOtherMapping
          }
        case _ => false

    override def hashCode(): Int =
      val snapshotMapping = addrTrans.mapping
      try {
        addrTrans.mapping = this.addrTransState
        this.effectState.hashCode()
      } finally {
        addrTrans.mapping = snapshotMapping
      }

  final override type State = AddressClosureState

  override def getState: State =
    AddressClosureState(addrTrans.getState, effect.getState)

  override def setState(st: State): Unit =
    addrTrans.setState(st.addrTransState)
    effect.setState(st.effectState)

  override def mapState(st: State, f: [A] => A => A): State =
    val addrTransStateNew = addrTrans.mapState(st.addrTransState, f)
    val effectStateNew = effect.mapState(st.effectState, f)
    AddressClosureState(addrTransStateNew, effectStateNew)


  override def join: Join[State] =
    (v1: AddressClosureState, v2: AddressClosureState) =>
      val snapshotMapping = addrTrans.mapping
      val snapshotOtherMapping = addrTrans.otherMapping
      try {
        addrTrans.mapping = v1.addrTransState
        addrTrans.otherMapping = Some(v2.addrTransState)
        val j1 = addrTrans.join(v1.addrTransState, v2.addrTransState)
        val j2 = effect.join(v1.effectState, v2.effectState)
        MaybeChanged(AddressClosureState(j1.get, j2.get), j1.hasChanged || j2.hasChanged)
      } finally {
        addrTrans.mapping = snapshotMapping
        addrTrans.otherMapping = snapshotOtherMapping
      }
  override def widen: Widen[State] =
    (v1: AddressClosureState, v2: AddressClosureState) =>
      val snapshotMapping = addrTrans.mapping
      val snapshotOtherMapping = addrTrans.otherMapping
      try {
        addrTrans.mapping = v1.addrTransState
        addrTrans.otherMapping = Some(v2.addrTransState)
        val j1 = addrTrans.widen(v1.addrTransState, v2.addrTransState)
        val j2 = effect.widen(v1.effectState, v2.effectState)
        MaybeChanged(AddressClosureState(j1.get, j2.get), j1.hasChanged || j2.hasChanged)
      } finally {
        addrTrans.mapping = snapshotMapping
        addrTrans.otherMapping = snapshotOtherMapping
      }

case class VirtualAddress[Context](ctx: Context, n: Int, addressTrans: AddressTranslation[Context]) extends AbstractAddr[VirtualAddress[Context]]:

  def physical: PowPhysicalAddress[Context] =
    addressTranslation(ctx, n)

  override def isEmpty: Boolean = false
  override def isStrong: Boolean = physical.isStrong
  override def reduce[A](f: VirtualAddress[Context] => A)(using Join[A]): A = f(this)
  override def iterator: Iterator[VirtualAddress[Context]] = Iterator(this)
  override def toString: String = s"Virtual($ctx, $n)"

  final override def equals(obj: Any): Boolean =
    obj match
      case other: VirtualAddress[Context] =>
        addressTrans.isEqual(this, other)
      case _ => false

  final override def hashCode(): Int =
    physical.hashCode()

  final def identifier: (Context,Int) = (ctx,n)

  final def addressTranslation: AddressTranslation[Context] = addressTrans

given VirtualAddressOrdering[Context : Ordering]: Ordering[VirtualAddress[Context]] =
  Ordering.by(virt => virt.identifier)

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
      case (PowRecency.RecentOld, PowRecency.RecentOld) => Unchanged(PowRecency.RecentOld)
      case (_,_) => Changed(PowRecency.RecentOld)


type PowPhysicalAddress[Context] = PowersetAddr[PhysicalAddress[Context], PhysicalAddress[Context]]

/**
 * Set of virtual addresses supporting mutating hashcodes.
 *
 * Invariant: `addrs` is empty iff `addressMap` is `None`.
 * Assumption: The address map of all virtual addresses is the same.
 */
class PowVirtualAddress[Context](val addrs: Map[Context, Set[Int]], val addressMap: Option[AddressTranslation[Context]]) extends AbstractAddr[VirtualAddress[Context]]:
  def virtualAddresses: Iterable[VirtualAddress[Context]] =
    addressMap match
      case Some(addrMap) =>  addrs.flatMap((ctx,idxs) => idxs.map(idx => VirtualAddress(ctx, idx, addrMap)))
      case None => Iterable.empty

  def physicalAddresses: Set[PhysicalAddress[Context]] = virtualAddresses.flatMap(_.physical.addrs).toSet

  override def isEmpty: Boolean = addrs.isEmpty

  override def isStrong: Boolean = virtualAddresses.forall(addr => addr.isStrong)

  override def reduce[A](f: VirtualAddress[Context] => A)(using Join[A]): A =
    virtualAddresses.map(f).reduce((a,b) => Join(a,b).get)

  override def iterator: Iterator[VirtualAddress[Context]] =
    virtualAddresses.iterator

  override def toString: String =
    virtualAddresses.toString

  override def equals(obj: Any): Boolean =
    obj match
      case other: PowVirtualAddress[?] =>
        this.addrs.equals(other.addrs)

  override def hashCode(): Int = addrs.hashCode()

object PowVirtualAddress:
  def empty[Context]: PowVirtualAddress[Context] = new PowVirtualAddress[Context](Map.empty, None)
  def apply[Context](virtualAddresses: VirtualAddress[Context]*): PowVirtualAddress[Context] =
    apply(virtualAddresses)
  def apply[Context](virtualAddresses: Iterable[VirtualAddress[Context]]): PowVirtualAddress[Context] =
    var addrs = Map.empty[Context, Set[Int]]
    var addrMap: Option[AddressTranslation[Context]] = None
    for (virt <- virtualAddresses) {
      val (ctx, n) = virt.identifier
      addrs += ctx -> (addrs.getOrElse(ctx, Set.empty[Int]) + n)
      addrMap = Some(virt.addressTranslation)
    }
    new PowVirtualAddress(addrs, addrMap)

given CombinePowVirtualAddress[W <: Widening, Context]: Combine[PowVirtualAddress[Context], W] with
  override def apply(v1: PowVirtualAddress[Context], v2: PowVirtualAddress[Context]): MaybeChanged[PowVirtualAddress[Context]] =
    (v1.addressMap,v2.addressMap) match
      case (None, None) => Unchanged(v1)
      case (Some(_), None) => Changed(v1)
      case (None, Some(_)) => Changed(v2)
      case (Some(addrMap1), Some(_)) =>
        var joined = v1.addrs
        var changed = false
        for ((ctx2, indices2) <- v2.addrs)
          joined.get(ctx2) match
            case None =>
              joined += ctx2 -> indices2
              changed = true
            case Some(indices1) =>
              if(! changed) {
                val physicals1 = indices1.map(idx => addrMap1(ctx2,idx))
                val physicals2 = indices2.map(idx => addrMap1(ctx2,idx))
                if (physicals1 != physicals2)
                  changed = true
              }
              joined += ctx2 -> indices1.union(indices2)
        MaybeChanged(new PowVirtualAddress(joined, v1.addressMap), changed)

given PowVirtAddrOrdering[Context]: PartialOrder[PowVirtualAddress[Context]] with
  override def lteq(x: PowVirtualAddress[Context], y: PowVirtualAddress[Context]): Boolean =
    x.physicalAddresses.subsetOf(y.physicalAddresses)