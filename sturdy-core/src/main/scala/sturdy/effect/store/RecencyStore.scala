package sturdy.effect.store

import sturdy.data.{JOption, JOptionA, WithJoin, given}
import sturdy.effect.allocation.Allocation
import sturdy.effect.store.{AStoreGenericThreadded, ManageableAddr, Store}
import sturdy.effect.{ComputationJoiner, Stateless, TrySturdy}
import sturdy.values
import sturdy.values.{Finite, *}

import scala.collection.immutable.{HashMap, IntMap}
import scala.collection.{MapView, mutable}
import scala.reflect.ClassTag

enum Recency:
  case Recent
  case Old

import sturdy.effect.store.Recency.*

class VirtualAddress[Context](val ctx: Context, val n: Int,
                              addressTranslation: (Context,Int) => PhysicalAddress[Context]):

  override def equals(obj: Any): Boolean =
    obj match
      case other: VirtualAddress[?] => this.lookupPhysicalAddress == other.lookupPhysicalAddress
      case _ => false

  override def hashCode(): Int =
    lookupPhysicalAddress.hashCode()

  def lookupPhysicalAddress: PhysicalAddress[Context] =
    addressTranslation(ctx,n)

case class PhysicalAddress[Context](ctx: Context, recency: Recency) extends
  ManageableAddr(false)

given finitePhysicalAddr[Context]: Finite[PhysicalAddress[Context]] with {}

class RecencyStore[Context, V](using Join[V], Widen[V], Finite[Context])
  extends Allocation[VirtualAddress[Context], Context],
          Store[VirtualAddress[Context], V, WithJoin]:

  protected var store: Map[PhysicalAddress[Context], V] = Map()
  protected var addressTranslation: Map[(Context, Int), PhysicalAddress[Context]] = Map()
  protected var mostRecent: HashMap[Context, Set[Int]] = HashMap()
  protected var next: Int = 0
  def getNext() = { next += 1; next }

  protected def lookupPhysicalAddress(ctx: Context, n: Int): PhysicalAddress[Context] =
    addressTranslation(ctx,n)

  def alloc(ctx: Context): VirtualAddress[Context] =
    apply(ctx)

  override def apply(ctx: Context): VirtualAddress[Context] =
    val fresh = getNext()
    mostRecent.get(ctx) match
      case Some(oldVirts) =>
        mostRecent += ctx -> Set(fresh)
        val virt = VirtualAddress(ctx, fresh, lookupPhysicalAddress)
        val phys = PhysicalAddress(ctx, Recent)
        addressTranslation += (ctx,fresh) -> phys
        this.read(PhysicalAddress(ctx, Recent)).map { oldVal =>
          weakUpdate(PhysicalAddress(ctx, Old), oldVal)
        }
        for(oldVirt <- oldVirts)
          addressTranslation += ((ctx,oldVirt)) -> PhysicalAddress(ctx,Old)
        virt
      case None =>
        mostRecent += ctx -> Set(fresh)
        val virt = VirtualAddress(ctx, fresh, lookupPhysicalAddress)
        val phys = PhysicalAddress(ctx, Recent)
        addressTranslation += (ctx,fresh) -> phys
        virt

  override def read(virt: VirtualAddress[Context]): JOption[WithJoin, V] =
    read(virt.lookupPhysicalAddress)

  private def read(phys: PhysicalAddress[Context]): JOption[WithJoin, V] =
    store.get(phys) match
      case scala.None =>
        JOptionA.none
      case scala.Some(v) =>
        if (phys.isManaged)
          JOptionA.some(v)
        else
          JOptionA.noneSome(v)

  def write(virt: VirtualAddress[Context], v: V): Unit =
    val phys = virt.lookupPhysicalAddress
    phys.recency match
      case Recent => strongUpdate(phys, v)
      case Old => weakUpdate(phys, v)

  def weakUpdate(x: PhysicalAddress[Context], v: V): Unit =
    store.get(x) match
      case None => store += x -> v
      case Some(old) => Join(old, v).ifChanged(store += x -> _)

  def strongUpdate(x: PhysicalAddress[Context], v: V): Unit =
    store += x -> v

  override def free(virt: VirtualAddress[Context]): Unit =
    val phys = virt.lookupPhysicalAddress
    phys.recency match
      case Recent =>
        addressTranslation -= (virt.ctx,virt.n)
        store -= phys
      case Old => // do nothing

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new RecencyStoreJoiner)

  private class RecencyStoreJoiner[A] extends ComputationJoiner[A]:
    private val snapshot: RecencyStoreState = getState
    private var firstBranch: RecencyStoreState = _

    override def inbetween(): Unit =
      firstBranch = getState
      setState(snapshot)

    override def retainNone(): Unit =
      setState(snapshot)

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      setState(firstBranch)

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      // The current state is already the state of the second branch.
      // Nothing to do.
      unit

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      setState(join(firstBranch, getState).get)

  override type State = RecencyStoreState

  case class RecencyStoreState(store: Map[PhysicalAddress[Context], V],
                               addrTrans: Map[(Context, Int), PhysicalAddress[Context]],
                               mostRecent: HashMap[Context, Set[Int]])

  override def getState: RecencyStoreState =
    RecencyStoreState(this.store, this.addressTranslation, this.mostRecent)

  override inline def setState(st: RecencyStoreState): Unit =
    store = st.store
    addressTranslation = st.addrTrans
    mostRecent = st.mostRecent

  override def join: Join[RecencyStoreState] = new CombineRecencyStoreState
  override def widen: Widen[RecencyStoreState] = new CombineRecencyStoreState

  private class CombineRecencyStoreState[W <: Widening](using Combine[V, W]) extends Combine[RecencyStoreState, W]:
    override def apply(state1: RecencyStoreState, state2: RecencyStoreState): MaybeChanged[RecencyStoreState] =
      var store: Map[PhysicalAddress[Context], V] = Map()
      var addrTrans: Map[(Context, Int), PhysicalAddress[Context]] = Map()
      var mostRecent: HashMap[Context, Set[Int]] = HashMap()
      var changed = false

      def updateAddrTrans(virt: (Context,Int), phys: PhysicalAddress[Context]): Unit =
        addrTrans += virt -> phys
        updateMostRecent(virt, phys)

      def updateMostRecent(virt: (Context,Int), phys: PhysicalAddress[Context]): Unit =
        if (phys.recency == Recent)
          val (ctx,n) = virt
          mostRecent += ctx -> (mostRecent.getOrElse(ctx, Set()) + n)

      // Update store at physical address `phys` by joining optional values `o1` and `o2`.
      def updateStore(phys: PhysicalAddress[Context], o1: Option[V], o2: Option[V]): Unit =
        val joinArgs = Join(JOptionA(o1),JOptionA(o2))
        joinArgs.get.map(v =>
          if (store.contains(phys))
            store += phys -> Join(store(phys), v).get
          else
            store += phys -> v
        )
        changed ||= joinArgs.hasChanged


      // This loop binds all virtual addresses in state1.addrTrans and state2.addrTrans
      // Furthermore, it joins store bindings of physical addresses bound by the final address translation.
      for(virt <- state1.addrTrans.keySet.union(state2.addrTrans.keySet))
        (state1.addrTrans.get(virt), state2.addrTrans.get(virt)) match
          case (Some(phys1), None) =>
            // This case can happen if state2 frees a recent address.
            updateAddrTrans(virt, phys1)
            updateStore(phys1, state1.store.get(phys1), state1.store.get(phys1))
          case (None, Some(phys2)) =>
            // This case can happen if state2 allocates an address for a context not bound in state1
            updateAddrTrans(virt, phys2)
            updateStore(phys2, state1.store.get(phys2), state2.store.get(phys2))
          case (Some(phys1), Some(phys2)) if phys1 == phys2 =>
            updateAddrTrans(virt, phys1)
            updateStore(phys1, state1.store.get(phys1), state2.store.get(phys1))
          case (Some(phys1@PhysicalAddress(ctx1,Old)), Some(phys2@PhysicalAddress(ctx2,Recent))) if ctx1 == ctx2 =>
            updateAddrTrans(virt, phys1)
            // The order of arguments state1.store.get(phys1) and state2.store.get(phys2) is important.
            // The other way around leads to the join not detecting that the result has stabilized.
            updateStore(phys1, state1.store.get(phys1), state2.store.get(phys2))
          case (Some(phys1@PhysicalAddress(ctx1,Recent)), Some(phys2@PhysicalAddress(ctx2,Old))) if ctx1 == ctx2 =>
            // This case can happen if state2 allocates a new virtual address for ctx2.
            updateAddrTrans(virt, phys2)
            // The order of arguments state2.store.get(phys2) and state1.store.get(phys1) is important.
            // The other way around leads to the join not detecting that the result has stabilized.
            updateStore(phys2, state2.store.get(phys2), state1.store.get(phys1))
          case (Some(phys1), Some(phys2)) /* if phys1.ctx != phys2.ctx */ =>
            throw new IllegalStateException(s"virtual address ${virt} maps to physical addresses with different contexts: ${phys1}, ${phys2}")
          case (None, None) =>
            throw new IllegalStateException(s"This case cannot happen. The virtual address must either be bound in state1.addrTrans or state2.addrTrans")

      MaybeChanged(RecencyStoreState(store, addrTrans, mostRecent), changed)

//class RecencyAbstractly[Addr, Context](c: CAllocationIntIncrement[Context], addr: Context => Addr) extends Abstractly[(Context,Int), Addr]:
//  override def apply(caddr: (Context,Int)): Addr = addr(caddr._1)
