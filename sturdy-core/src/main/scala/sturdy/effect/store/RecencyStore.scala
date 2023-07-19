package sturdy.effect.store

import sturdy.data.{JOption, JOptionA, WithJoin}
import sturdy.effect.allocation.Allocation
import sturdy.effect.store.{AStoreGenericThreadded, ManageableAddr, Store}
import sturdy.effect.{ComputationJoiner, Stateless, TrySturdy}
import sturdy.values
import sturdy.values.{Finite, *}

import scala.collection.immutable.{HashMap, IntMap}
import scala.collection.{MapView, mutable}
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
  protected var dirtyAddrs: Set[PhysicalAddress[Context]] = Set()
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
    dirtyAddrs += x
    store.get(x) match
      case None => store += x -> v
      case Some(old) => Join(old, v).ifChanged(store += x -> _)

  def strongUpdate(x: PhysicalAddress[Context], v: V): Unit =
    dirtyAddrs += x
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
    private val snapshotStore = store
    private val snapshotDirtyAddrs = dirtyAddrs
    private val snapshotAddrTrans = addressTranslation
    private val snapshotMostRecent = mostRecent

    private var firstBranchStore: Map[PhysicalAddress[Context], V] = _
    private var firstBranchDirtyAddrs: Set[PhysicalAddress[Context]] = _
    private var firstBranchAddrTrans: Map[(Context,Int), PhysicalAddress[Context]] = _
    private var firstBranchMostRecent: HashMap[Context, Set[Int]] = _

    dirtyAddrs = Set()

    override def inbetween(): Unit =
      firstBranchStore = store
      firstBranchDirtyAddrs = dirtyAddrs
      firstBranchAddrTrans = addressTranslation
      firstBranchMostRecent = mostRecent

      store = snapshotStore
      dirtyAddrs = Set()
      addressTranslation = snapshotAddrTrans
      mostRecent = snapshotMostRecent

    override def retainNone(): Unit =
      store = snapshotStore
      dirtyAddrs = snapshotDirtyAddrs
      addressTranslation = snapshotAddrTrans
      mostRecent = snapshotMostRecent

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      store = firstBranchStore
      dirtyAddrs = snapshotDirtyAddrs ++ firstBranchDirtyAddrs
      addressTranslation = firstBranchAddrTrans
      mostRecent = firstBranchMostRecent

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      dirtyAddrs ++= snapshotDirtyAddrs

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      for (x <- firstBranchDirtyAddrs)
        weakUpdate(x, firstBranchStore(x))

      def maybeUpdateStore(phys: PhysicalAddress[Context], o1: Option[V], o2: Option[V]): Unit =
        (o1, o2) match
          case (None, None) => // Do nothing
          case (Some(v1), None) =>
            store += phys -> v1
            dirtyAddrs += phys
          case (None, Some(v2)) =>
            store += phys -> v2
            dirtyAddrs += phys
          case (Some(v1), Some(v2)) =>
            val join = Join(v1, v2)
            store += phys -> join.get
            dirtyAddrs += phys

      for((virt1,phys1) <- firstBranchAddrTrans)
        addressTranslation.get(virt1) match
          case None =>
            addressTranslation += virt1 -> phys1
          case Some(phys2) =>
            (phys1,phys2) match
              case (PhysicalAddress(ctx1,Recent),PhysicalAddress(ctx2,Old)) if ctx1 == ctx2 =>
                maybeUpdateStore(phys2, firstBranchStore.get(phys1), store.get(phys2))
              case (PhysicalAddress(ctx1,Old), PhysicalAddress(ctx2,Recent)) if ctx1 == ctx2 =>
                maybeUpdateStore(phys1, firstBranchStore.get(phys1), store.get(phys2))
              case (_,_) if phys1 == phys2 =>
                maybeUpdateStore(phys1, firstBranchStore.get(phys1), store.get(phys2))
              case (_,_) /* if phys1 != phys2 */ =>
                throw new IllegalStateException(s"virtual address ${virt1} maps to physical addresses with different contexts: ${phys1}, ${phys2}")
      for((ctx,oldVirts) <- firstBranchMostRecent)
        mostRecent += ctx -> mostRecent.getOrElse(ctx,Set()).union(oldVirts)

  override type State = RecencyStoreState

  case class RecencyStoreState(store: Map[PhysicalAddress[Context], V],
                               addrTrans: Map[(Context, Int), PhysicalAddress[Context]],
                               mostRecent: HashMap[Context, Set[Int]])


  override def getState: State =
    RecencyStoreState(this.store, this.addressTranslation, this.mostRecent)

  override def setState(st: RecencyStoreState): Unit =
    store = st.store
    addressTranslation = st.addrTrans
    mostRecent = st.mostRecent

  override def join: Join[RecencyStoreState] = (v1: RecencyStoreState, v2: RecencyStoreState) =>
    var store: Map[PhysicalAddress[Context], V] = Map()
    var addrTrans: Map[(Context, Int), PhysicalAddress[Context]] = Map()
    val mostRecent: HashMap[Context, Set[Int]] = v1.mostRecent.merged(v2.mostRecent){case ((ctx1,rec1),(_,rec2)) => (ctx1, rec1.union(rec2))}
    var changed = false

    def maybeUpdateStore(phys: PhysicalAddress[Context], o1: Option[V], o2: Option[V]): Unit =
      (o1, o2) match
        case (None, None) => // Do nothing
        case (Some(v1), None) =>
          store += phys -> v1
          changed = true
        case (None, Some(v2)) =>
          store += phys -> v2
          changed = true
        case (Some(v1), Some(v2)) =>
          val join = Join(v1, v2)
          store += phys -> join.get
          changed ||= join.hasChanged

    for(virt <- v1.addrTrans.keySet.union(v2.addrTrans.keySet))
      (v1.addrTrans.get(virt), v2.addrTrans.get(virt)) match
        case (Some(phys),None) =>
          addrTrans += virt -> phys
          maybeUpdateStore(phys, v1.store.get(phys), None)
          changed = true
        case (None, Some(phys)) =>
          addrTrans += virt -> phys
          maybeUpdateStore(phys, None, v2.store.get(phys))
          changed = true
        case (Some(phys1@PhysicalAddress(ctx1,Recent)), Some(phys2@PhysicalAddress(ctx2,Old))) if ctx1 == ctx2 =>
          addrTrans += virt -> phys2
          maybeUpdateStore(phys2, v1.store.get(phys1), v2.store.get(phys2))
          changed = true
        case (Some(phys1@PhysicalAddress(ctx1,Old)), Some(phys2@PhysicalAddress(ctx2,Recent))) if ctx1 == ctx2 =>
          addrTrans += virt -> phys1
          maybeUpdateStore(phys1, v1.store.get(phys1), v2.store.get(phys2))
          changed = true
        case (Some(phys1), Some(phys2)) if phys1 == phys2 =>
          addrTrans += virt -> phys1
          maybeUpdateStore(phys1, v1.store.get(phys1), v2.store.get(phys2))
        case (Some(phys1), Some(phys2)) /* if phys1.ctx != phys2.ctx */ =>
          throw new IllegalStateException(s"virtual address ${virt} maps to physical addresses with different contexts: ${phys1}, ${phys2}")
        case (None, None) =>
          throw new IllegalStateException(s"virtual address ${virt} appear in neither address translation map")

    MaybeChanged(RecencyStoreState(store, addrTrans, mostRecent), changed)

  override def widen: Widen[RecencyStoreState] = ???

//class RecencyAbstractly[Addr, Context](c: CAllocationIntIncrement[Context], addr: Context => Addr) extends Abstractly[(Context,Int), Addr]:
//  override def apply(caddr: (Context,Int)): Addr = addr(caddr._1)
