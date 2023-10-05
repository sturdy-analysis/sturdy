package sturdy.effect.store

import sturdy.values.references.{AbstractAddr, PowersetAddr}
import sturdy.values.{Changed, Combine, Finite, Join, MaybeChanged, Structural, Unchanged, Widening}

class VirtualAddress[Context](val ctx: Context, val n: Int,
                              val currentPhysical: VirtualAddress[Context] => PowPhysicalAddress[Context])
  extends AbstractAddr[VirtualAddress[Context]]:

  def physical: PowPhysicalAddress[Context] = currentPhysical(this)
  override def isEmpty: Boolean = false
  override def isStrong: Boolean = physical.isStrong
  override def reduce[A](f: VirtualAddress[Context] => A)(using Join[A]): A = f(this)

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
class PowVirtualAddress[Context](val addrs: Map[Context, Set[Int]], val addressMap: Option[VirtualAddress[Context] => PowPhysicalAddress[Context]]) extends AbstractAddr[VirtualAddress[Context]]:
  def virtualAddresses: Iterable[VirtualAddress[Context]] =
    addressMap match
      case Some(addrMap) =>  addrs.flatMap((ctx,idxs) => idxs.map(idx => VirtualAddress(ctx, idx, addrMap)))
      case None => Iterable.empty

  def physicalAddresses: Set[PhysicalAddress[Context]] = virtualAddresses.flatMap(_.physical.addrs).toSet

  override def isEmpty: Boolean = addrs.isEmpty

  override def isStrong: Boolean = virtualAddresses.forall(addr => addr.isStrong)

  override def reduce[A](f: VirtualAddress[Context] => A)(using Join[A]): A =
    virtualAddresses.map(f).reduce((a,b) => Join(a,b).get)

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
    var addrs = Map.empty[Context, Set[Int]]
    var addrMap: Option[VirtualAddress[Context] => PowPhysicalAddress[Context]] = None
    for(virt <- virtualAddresses) {
      addrs += virt.ctx -> (addrs.getOrElse(virt.ctx, Set.empty[Int]) + virt.n)
      addrMap = Some(virt.currentPhysical)
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

