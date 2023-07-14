package sturdy.effect.allocation

import sturdy.effect.Stateless
import sturdy.values.{Abstractly, Finite}

import scala.collection.immutable.IntMap
import scala.collection.mutable
import scala.reflect.ClassTag
import sturdy.effect.store.ManageableAddr

enum Recency:
  case Recent
  case Old

import Recency.*

class VirtualAddress[Context](val ctx: Context, val n: Int, addressTranslation: mutable.Map[(Context,Int),PhysicalAddress[Context]]):

  override def equals(obj: Any): Boolean =
    obj match
      case other: VirtualAddress[?] =>
        try   { this.lookupPhysicalAddress == other.lookupPhysicalAddress.asInstanceOf[PhysicalAddress[Context]] }
        catch { case _: ClassCastException => false }
      case _ => false

  override def hashCode(): Int =
    addressTranslation(this.toTuple).hashCode()

  def lookupPhysicalAddress: PhysicalAddress[Context] =
    addressTranslation(this.toTuple)

  def toTuple: (Context,Int) = (ctx,n)

case class PhysicalAddress[Context](ctx: Context, recency: Recency)
  extends ManageableAddr(false)

given finitePhysicalAddr[Context](using finitCtx: Finite[Context]): Finite[PhysicalAddress[Context]] with {}

class RecencyAllocator[Context] extends Allocation[VirtualAddress[Context], Context], Stateless:
  private val addressTranslation: mutable.Map[(Context,Int), PhysicalAddress[Context]] = mutable.Map()
  private val next: mutable.Map[Context, Int] = mutable.Map()
  private val observers: mutable.ArrayBuffer[AllocationObserver[Context,VirtualAddress[Context]]] =
    mutable.ArrayBuffer()

  override def apply(ctx: Context): VirtualAddress[Context] =
    val newAddr = next.get(ctx) match
      case Some(n) =>
        next += ctx -> (n + 1)
        val virt = VirtualAddress(ctx, n, addressTranslation)
        val phys = PhysicalAddress(ctx, Recent)
        addressTranslation += virt.toTuple -> phys
        addressTranslation((ctx,n-1)) = PhysicalAddress(ctx,Old)
        virt
      case None =>
        next += ctx -> 2
        val virt = VirtualAddress(ctx, 1, addressTranslation)
        val phys = PhysicalAddress(ctx, Recent)
        addressTranslation += virt.toTuple -> phys
        virt

    notifyObservers(ctx, newAddr)

    newAddr

  def addAllocationObserver(observer: AllocationObserver[Context, VirtualAddress[Context]]): Unit =
    observers += observer

  private def notifyObservers(ctx: Context, newAddr: VirtualAddress[Context]) =
    for (observer <- observers)
      observer(ctx, newAddr)

type AllocationObserver[Context,Addr] = (Context, Addr) => Unit

//class RecencyAbstractly[Addr, Context](c: CAllocationIntIncrement[Context], addr: Context => Addr) extends Abstractly[(Context,Int), Addr]:
//  override def apply(caddr: (Context,Int)): Addr = addr(caddr._1)
