package sturdy.values.references

import sturdy.data.given
import sturdy.effect.Effect
import sturdy.effect.store.*
import sturdy.values.*
import sturdy.values.references.{AbstractAddr, PowersetAddr, given}

class AddressTranslation[Context](init: Map[(Context,Int), PowRecency]) extends Effect:
  var mapping: Map[(Context,Int), PowRecency] = init
  given finiteVirt: Finite[(Context,Int)] with {}
  given finitePowRecency: Finite[PowRecency] with {}

  def apply(virt: VirtualAddress[Context]): PowPhysicalAddress[Context] =
    mapping.get((virt.ctx, virt.n)) match
      case Some(recencies) => new PowPhysicalAddress(Map((virt.ctx, virt.n) -> recencies), Some(this))
      case None => throw IllegalStateException(s"Virtual address $virt is not bound to a physical address")

  def += (kv: ((Context,Int), PowRecency)): Unit =
    mapping += kv

  def -=(virt: (Context, Int)): Unit =
    mapping -= virt

  override type State = Map[(Context,Int), PowRecency]

  override def getState: State = mapping

  override def setState(st: State): Unit =
    mapping = st

  override def join: Join[State] = implicitly[Join[State]]

  override def widen: Widen[State] = implicitly[Widen[State]]

  def virtualAddresses: PowVirtualAddress[Context] =
    PowVirtualAddress(mapping.keySet.view.map((ctx,n) => VirtualAddress(ctx, n, this)))

  def virtualAddresses(ctx: Context): PowVirtualAddress[Context] =
    PowVirtualAddress(mapping.keySet.view.filter(_._1 == ctx).map((ctx,n) => VirtualAddress(ctx, n, this)))

  def oldAddresses(ctx: Context): PowPhysicalAddress[Context] =
    new PowPhysicalAddress(mapping.filter{ case ((c,idx),recencies) =>
      c == ctx && (recencies == PowRecency.Old || recencies == PowRecency.RecentOld)
    }, Some(this))

  def physicalAddressesByContext: Map[Context, PowPhysicalAddress[Context]] = ???
//    mapping.groupMapReduce(_._1._1){
//        case ((ctx, _), PowRecency.Old) => PowersetAddr(PhysicalAddress(ctx, Recency.Old))
//        case ((ctx, _), PowRecency.Recent) => PowersetAddr(PhysicalAddress(ctx, Recency.Recent))
//        case ((ctx, _), PowRecency.RecentOld) => PowersetAddr(PhysicalAddress(ctx, Recency.Old), PhysicalAddress(ctx, Recency.Recent))
//      }(Join.compute)

  def virtualAddressesByContext: Map[Context, PowVirtualAddress[Context]] = ???
//    mapping.groupMapReduce(_._1._1){
//        case ((ctx, n), _) => PowVirtualAddress(VirtualAddress(ctx, n, this))
//      }(Join.compute)

  override def clone(): AddressTranslation[Context] = new AddressTranslation[Context](mapping)

object AddressTranslation:
  def empty[Context]: AddressTranslation[Context] = new AddressTranslation[Context](Map.empty)

class VirtualAddress[Context](val ctx: Context, val n: Int, val addressTranslation: AddressTranslation[Context])
  extends AbstractAddr[VirtualAddress[Context]]:

  def physical: PowPhysicalAddress[Context] = addressTranslation(this)
  override def isEmpty: Boolean = false
  override def isStrong: Boolean = physical.isStrong
  override def reduce[A](f: VirtualAddress[Context] => A)(using Join[A]): A = f(this)
  override def iterator: Iterator[VirtualAddress[Context]] = Iterator(this)

  override def toString: String = s"VirtualAddress($ctx, $n)"

  final override def equals(obj: Any): Boolean =
    obj match
      case other: VirtualAddress[?] => this.physical == other.physical
      case _ => false


  final override def hashCode(): Int =
    physical.hashCode()

  final def identifier: (Context,Int) = (ctx,n)

case class PhysicalAddress[Context](ctx: Context, recency: Recency) extends AbstractAddr[PhysicalAddress[Context]]:
  override def isEmpty: Boolean = false
  override def isStrong: Boolean = recency == Recency.Recent
  override def reduce[A](f: PhysicalAddress[Context] => A)(using Join[A]): A = f(this)
  override def iterator: Iterator[PhysicalAddress[Context]] = Iterator(this)
  override def toString(): String = s"PhysicalAddress($ctx,$recency)"
  override def equals(obj: Any): Boolean =
    obj match
      case other: PhysicalAddress[?] =>
        this.ctx == other.ctx && this.recency == other.recency
  override def hashCode(): Int = (ctx,recency).hashCode()

given finitePhysicalAddr[Context](using Finite[Context]): Finite[PhysicalAddress[Context]] with {}

given finiteVirtualAddr[Context](using Finite[PhysicalAddress[Context]]): Finite[VirtualAddress[Context]] with {}
given structuralVirtualAddr[Context]: Structural[VirtualAddress[Context]] with {}

enum Recency:
  case Recent
  case Old

enum PowRecency:
  case Recent
  case Old
  case RecentOld

  def map[B](f: Recency => B): IterableOnce[B] =
    this match
      case PowRecency.Recent => Iterable(f(Recency.Recent))
      case PowRecency.Old => Iterable(f(Recency.Old))
      case PowRecency.RecentOld => Iterable(f(Recency.Recent), f(Recency.Old))

given CombinePowRencency[W <: Widening]: Combine[PowRecency, W] with
  override def apply(v1: PowRecency, v2: PowRecency): MaybeChanged[PowRecency] =
    (v1,v2) match
      case (PowRecency.Recent, PowRecency.Recent) => Unchanged(PowRecency.Recent)
      case (PowRecency.Old, PowRecency.Old) => Unchanged(PowRecency.Old)
      case (PowRecency.RecentOld, PowRecency.RecentOld) => Unchanged(PowRecency.RecentOld)
      case (_,_) => Changed(PowRecency.RecentOld)

class PowPhysicalAddress[Context](val addrs: Map[(Context,Int), PowRecency], val addressMap: Option[AddressTranslation[Context]])
  extends AbstractAddr[PhysicalAddress[Context]]:
  def virtualPhysical: Iterable[(VirtualAddress[Context], PhysicalAddress[Context])] =
    addressMap match
      case Some(addrMap) =>
        for{
          ((ctx,idx), recencies) <- addrs
          recency <- recencies
        } yield (VirtualAddress(ctx, idx, addrMap), PhysicalAddress(ctx, recency))
      case None => Iterable.empty

  def physicalAddresses: Iterable[PhysicalAddress[Context]] =
    for{
      ((ctx,_), recencies) <- addrs
      recency <- recencies
    } yield (PhysicalAddress(ctx, recency))

  override def isEmpty: Boolean = addrs.isEmpty
  override def isStrong: Boolean = addrs.forall((_,recencies) => recencies == PowRecency.Recent)
  override def iterator: Iterator[PhysicalAddress[Context]] = physicalAddresses.iterator

  def subsetOf(other: PowPhysicalAddress[Context]): Boolean =
    this.physicalAddresses.toSet.subsetOf(other.physicalAddresses.toSet)

  override def toString: String = s"PowPhysicalAddress($virtualPhysical)"

  override def equals(obj: Any): Boolean =
    obj match
      case other: PowPhysicalAddress[?] =>
        this.physicalAddresses.toSet == other.physicalAddresses.toSet
      case other: PowersetAddr[PhysicalAddress[?], PhysicalAddress[?]] =>
        this.physicalAddresses.toSet == other.addrs
      case _ => false
  override def hashCode(): Int = physicalAddresses.toSet.hashCode()

object PowPhysicalAddress:
  private val _empty: PowPhysicalAddress[Nothing] = new PowPhysicalAddress(Map.empty, None)
  def empty[Context]: PowPhysicalAddress[Context] = _empty.asInstanceOf
  def apply[Context](virts: Iterable[VirtualAddress[Context]]): PowPhysicalAddress[Context] =
    var addressMap: Option[AddressTranslation[Context]] = virts.headOption.map(_.addressTranslation)
    val addrs: Iterable[((Context, Int), PowRecency)] =
      for virt <- virts
      yield (virt.ctx, virt.n) -> virt.addressTranslation.mapping((virt.ctx, virt.n))
    new PowPhysicalAddress[Context](addrs.toMap, addressMap)

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

  def physicalAddresses: PowPhysicalAddress[Context] =
    addressMap match
      case Some(addrMap) =>
        new PowPhysicalAddress[Context](for ((ctx,idxs) <- addrs; idx <- idxs) yield ((ctx,idx), addrMap.mapping(ctx,idx)), addressMap)
      case None =>
        PowPhysicalAddress.empty

  override def isEmpty: Boolean = addrs.isEmpty
  override def isStrong: Boolean = virtualAddresses.forall(addr => addr.isStrong)
  override def iterator: Iterator[VirtualAddress[Context]] = virtualAddresses.iterator

  override def toString: String =
    virtualAddresses.toString

  override def equals(obj: Any): Boolean =
    obj match
      case other: PowVirtualAddress[?] =>
        this.addrs.equals(other.addrs)

  override def hashCode(): Int = addrs.hashCode()

object PowVirtualAddress:
  def empty[Context]: PowVirtualAddress[Context] = new PowVirtualAddress[Context](Map.empty, None)
  def apply[Context](virtualAddresses: Iterable[VirtualAddress[Context]]): PowVirtualAddress[Context] =
    var addrs = Map.empty[Context, Set[Int]]
    var addrMap: Option[AddressTranslation[Context]] = None
    for (virt <- virtualAddresses) {
      addrs += virt.ctx -> (addrs.getOrElse(virt.ctx, Set.empty[Int]) + virt.n)
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
                val physicals1 = indices1.map(idx => VirtualAddress(ctx2, idx, addrMap1).physical)
                val physicals2 = indices2.map(idx => VirtualAddress(ctx2, idx, addrMap1).physical)
                if (physicals1 != physicals2)
                  changed = true
              }
              joined += ctx2 -> indices1.union(indices2)
        MaybeChanged(new PowVirtualAddress(joined, v1.addressMap), changed)

given orderingPowersetVirtAddr[Context]: PartialOrder[PowVirtualAddress[Context]] with
  override def lteq(x: PowVirtualAddress[Context], y: PowVirtualAddress[Context]): Boolean =
    x.physicalAddresses.subsetOf(y.physicalAddresses)
