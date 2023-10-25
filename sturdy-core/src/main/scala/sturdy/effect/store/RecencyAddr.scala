package sturdy.effect.store

import apron.*

import sturdy.values.references.{AbstractAddr, PowersetAddr}
import sturdy.values.{Changed, Combine, Finite, Join, MaybeChanged, Structural, Unchanged, Widening}

type PowPhysicalAddress[Context] = PowersetAddr[PhysicalAddress[Context], PhysicalAddress[Context]]
type PowVirtualAddress[Context] = PowersetAddr[VirtualAddress[Context], VirtualAddress[Context]]

class VirtualAddress[Context](val ctx: Context, val n: Int,
                              currentPhysical: VirtualAddress[Context] => PowPhysicalAddress[Context])
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

case class PhysicalAddress[Context](ctx: Context, recency: Recency) extends AbstractAddr[PhysicalAddress[Context]], apron.Var:
  override def isEmpty: Boolean = false
  override def isStrong: Boolean = recency == Recency.Recent
  override def reduce[A](f: PhysicalAddress[Context] => A)(using Join[A]): A = f(this)

  override def clone() = throw new NotImplementedError("ApronVar comparison")
  override def compareTo(other : apron.Var) : Int = 
    throw new NotImplementedError("ApronVar comparison")

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
