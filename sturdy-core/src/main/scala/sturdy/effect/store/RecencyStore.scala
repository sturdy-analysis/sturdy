package sturdy.effect.store

import sturdy.data.{JOption, JOptionA, WithJoin}
import sturdy.effect.allocation.Allocation
import sturdy.effect.store.{AStoreGenericThreadded, ManageableAddr, Store}
import sturdy.effect.{ComputationJoiner, Stateless}
import sturdy.values
import sturdy.values.*

import scala.collection.immutable.IntMap
import scala.collection.mutable
import scala.reflect.ClassTag

/**
 * - TODO: Free remove from address translation map
 * - TODO: Invariant: Ensure that all addresses in address translation are bound on the heap.
 *   This is violated when heaps are snapshotted during
 */

enum Recency:
  case Recent
  case Old

import sturdy.effect.store.Recency.*

class VirtualAddress[Context](val ctx: Context, val n: Int,
                              addressTranslation: mutable.Map[(Context,Int),PhysicalAddress[Context]]):

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

class RecencyStore[Context, V](_init: Map[PhysicalAddress[Context], V])
                              (using Join[V], Widen[V], Finite[PhysicalAddress[Context]])
  extends Allocation[VirtualAddress[Context], Context],
          AStoreGenericThreadded[PhysicalAddress[Context], V],
          Store[VirtualAddress[Context], V, WithJoin]:

  this.store = _init
  private val addressTranslation: mutable.Map[(Context, Int), PhysicalAddress[Context]] = mutable.Map()
  private var mostRecent: Map[Context, Int] = Map()
  private var next: Int = 0

  def getNext() =
    next+= 1
    next

  override def apply(ctx: Context): VirtualAddress[Context] =
    val fresh = getNext()
    mostRecent.get(ctx) match
      case Some(oldCounter) =>
        mostRecent += ctx -> fresh
        val virt = VirtualAddress(ctx, fresh, addressTranslation)
        val phys = PhysicalAddress(ctx, Recent)
        addressTranslation += virt.toTuple -> phys
        addressTranslation((ctx,oldCounter)) = PhysicalAddress(ctx,Old)
        this.read(PhysicalAddress(ctx, Recent)).map { oldVal =>
          this.weakUpdate(PhysicalAddress(ctx, Old), oldVal)
        }
        virt
      case None =>
        mostRecent += ctx -> fresh
        val virt = VirtualAddress(ctx, fresh, addressTranslation)
        val phys = PhysicalAddress(ctx, Recent)
        addressTranslation += virt.toTuple -> phys
        virt


  override def read(x: VirtualAddress[Context]): JOption[WithJoin, V] =
    read(x.lookupPhysicalAddress)

  def read(x: PhysicalAddress[Context]): JOption[WithJoin, V] =
    store.get(x) match
      case scala.None => JOptionA.none
      case scala.Some(v) => JOptionA.noneSome(v)

  def write(x: VirtualAddress[Context], v: V): Unit =
    write(x.lookupPhysicalAddress, v)

  def write(x: PhysicalAddress[Context], v: V): Unit =
    x.recency match
      case Recent => strongUpdate(x, v)
      case Old => weakUpdate(x, v)

  override def free(x: VirtualAddress[Context]): Unit =
    free(x.lookupPhysicalAddress)

  def free(x: PhysicalAddress[Context]): Unit =
    x.recency match
      case Recent => store -= x
      case Old => // do nothing

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = ???

  case class RecencyStoreState(store: Map[PhysicalAddress[Context], V],
                               mostRecent: Map[Context, Int],
                               addrTrans: Map[(Context, Int), PhysicalAddress[Context]])

  override type State = RecencyStoreState

  override def getState: State =
    RecencyStoreState(super.getState, this.mostRecent, this.addressTranslation.toMap)

  override def setState(st: RecencyStoreState): Unit =
    super.setState(st.store)
    this.mostRecent = st.mostRecent
    this.addressTranslation.clear()
    this.addressTranslation.addAll(st.addrTrans)

  override def join: Join[RecencyStoreState] = (v1: RecencyStoreState, v2: RecencyStoreState) =>
    var addrTrans = v1.addrTrans
    var mostRecent = v1.mostRecent

    var changed = false
    for ((ctx, n2) <- v2.mostRecent)
      v1.mostRecent.get(ctx) match
        // Case where second branch allocated, but not first one and not before.
        case None =>
          mostRecent += ctx -> n2
          addrTrans += (ctx, n2) -> v2.addrTrans((ctx, n2))
          changed = true

        // Case where both branches did not allocate
        case Some(n1) if n1 == n2 => // do nothing, the snapshot is still valid.

        // Case where both branches allocated something
        case Some(n1) if n1 != n2 =>
          addrTrans += (ctx, n2) -> PhysicalAddress(ctx, Old)
          changed = true

    val storeJoined = super.join(v1.store, v2.store)
    changed ||= storeJoined.hasChanged

    if(changed)
      Changed(RecencyStoreState(storeJoined.get, mostRecent, addrTrans))
    else
      Unchanged(RecencyStoreState(storeJoined.get, mostRecent, addrTrans))

  override def widen: Widen[RecencyStoreState] = ???

//class RecencyAbstractly[Addr, Context](c: CAllocationIntIncrement[Context], addr: Context => Addr) extends Abstractly[(Context,Int), Addr]:
//  override def apply(caddr: (Context,Int)): Addr = addr(caddr._1)
