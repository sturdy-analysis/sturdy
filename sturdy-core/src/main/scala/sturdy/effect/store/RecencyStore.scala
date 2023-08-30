package sturdy.effect.store

import sturdy.data.{JOption, JOptionA, WithJoin, given}
import sturdy.effect.allocation.Allocation
import sturdy.effect.{ComputationJoiner, Stateless, TrySturdy}
import sturdy.values.{*, given}

import scala.collection.immutable.{HashMap, IntMap}
import scala.collection.{MapView, mutable}
import scala.reflect.ClassTag

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

class VirtualAddress[Context](val ctx: Context, val n: Int,
                              addressTranslation: (Context,Int) => Iterable[PhysicalAddress[Context]]):

  final override def equals(obj: Any): Boolean =
    obj match
      case other: VirtualAddress[?] => this.lookupPhysicalAddress == other.lookupPhysicalAddress
      case _ => false

  final override def hashCode(): Int =
    lookupPhysicalAddress.hashCode()

  final def identifier: (Context,Int) = (ctx,n)

  final def lookupPhysicalAddress: Set[PhysicalAddress[Context]] =
    addressTranslation(ctx,n).toSet

case class PhysicalAddress[Context](ctx: Context, recency: Recency) extends
  ManageableAddr(false)

given finitePhysicalAddr[Context]: Finite[PhysicalAddress[Context]] with {}

class RecencyStore[Context, V](val initStore: AStore[PhysicalAddress[Context], V])
                              (using Join[V], Widen[V], Finite[Context])
  extends Allocation[VirtualAddress[Context], Context],
          Store[VirtualAddress[Context], V, WithJoin]:

  protected var store: AStore[PhysicalAddress[Context], V] = initStore
  protected var addressTranslation: Map[(Context,Int), PowRecency] = Map()
  protected var mostRecent: Map[Context, Powerset[Int]] = HashMap()
  protected var next: Int = 0
  def getNext() = { next += 1; next }

  protected def lookupPhysicalAddress(ctx: Context, n: Int): Iterable[PhysicalAddress[Context]] =
    addressTranslation(ctx,n) match
      case PowRecency.Recent => Iterable(PhysicalAddress(ctx,Recency.Recent))
      case PowRecency.Old => Iterable(PhysicalAddress(ctx,Recency.Old))
      case PowRecency.RecentOld => Iterable(PhysicalAddress(ctx,Recency.Recent), PhysicalAddress(ctx,Recency.Old))

  def alloc(ctx: Context): VirtualAddress[Context] =
    apply(ctx)

  override def apply(ctx: Context): VirtualAddress[Context] =
    val fresh = getNext()
    mostRecent.get(ctx) match
      case Some(mostRecentVirts) =>
        mostRecent += ctx -> Powerset(fresh)
        val virt = VirtualAddress(ctx, fresh, lookupPhysicalAddress)
        addressTranslation += virt.identifier -> PowRecency.Recent
        store.read(PhysicalAddress(ctx, Recency.Recent)).map { oldVal =>
          store.weakUpdate(PhysicalAddress(ctx, Recency.Old), oldVal)
        }
        for(mostRecentVirt <- mostRecentVirts)
          addressTranslation += (ctx,mostRecentVirt) -> PowRecency.Old
        virt
      case None =>
        mostRecent += ctx -> Powerset(fresh)
        val virt = VirtualAddress(ctx, fresh, lookupPhysicalAddress)
        addressTranslation += virt.identifier -> PowRecency.Recent
        virt

  override def read(virt: VirtualAddress[Context]): JOptionA[V] =
    addressTranslation(virt.ctx,virt.n) match
      case PowRecency.Recent => store.read(PhysicalAddress(virt.ctx, Recency.Recent))
      case PowRecency.Old => store.read(PhysicalAddress(virt.ctx, Recency.Old))
      case PowRecency.RecentOld => Join(store.read(PhysicalAddress(virt.ctx, Recency.Old)), store.read(PhysicalAddress(virt.ctx, Recency.Old))).get

  def write(virt: VirtualAddress[Context], v: V): Unit =
    addressTranslation(virt.identifier) match
      case PowRecency.Recent => store.strongUpdate(PhysicalAddress(virt.ctx,Recency.Recent), v)
      case PowRecency.Old => store.weakUpdate(PhysicalAddress(virt.ctx,Recency.Old), v)
      case PowRecency.RecentOld =>
        store.weakUpdate(PhysicalAddress(virt.ctx, Recency.Old), v)
        store.weakUpdate(PhysicalAddress(virt.ctx, Recency.Recent), v)

  override def free(virt: VirtualAddress[Context]): Unit =
    val freedRecency = addressTranslation(virt.identifier)
    addressTranslation -= virt.identifier
    mostRecent += virt.ctx -> Powerset(mostRecent.getOrElse(virt.ctx, Powerset()).set - virt.n)
    freedRecency match
      case PowRecency.Recent | PowRecency.RecentOld =>
        if(addressTranslation.forall { case ((ctx, _), recency) => !(ctx == virt.ctx && recency == Recency.Recent) })
          store.delete(PhysicalAddress(virt.ctx, Recency.Recent))
      case PowRecency.Old => // do nothing

  override def join: Join[RecencyStoreState] = new CombineRecencyStoreState(initStore.join)
  override def widen: Widen[RecencyStoreState] = new CombineRecencyStoreState(initStore.widen)
  private class CombineRecencyStoreState[W <: Widening](combineStore: Combine[initStore.State, W])(using Combine[V, W]) extends Combine[RecencyStoreState, W]:
    override def apply(state1: RecencyStoreState, state2: RecencyStoreState): MaybeChanged[RecencyStoreState] =
      // TODO: I would like to use `initStore.combine`, but it does not exist.
      val combinedStores = combineStore(state1.store, state2.store)
      val combinedAddrTrans = Combine(state1.addrTrans, state2.addrTrans)
      val combinedMostRecent = Combine(state1.mostRecent, state2.mostRecent)
      MaybeChanged(RecencyStoreState(combinedStores.get, combinedAddrTrans.get, combinedMostRecent.get),
        hasChanged = combinedStores.hasChanged)

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

  case class RecencyStoreState(store: initStore.State,
                               addrTrans: Map[(Context,Int), PowRecency],
                               mostRecent: Map[Context, Powerset[Int]])

  override def getState: RecencyStoreState =
    RecencyStoreState(this.store.getState.asInstanceOf, this.addressTranslation, this.mostRecent)

  override inline def setState(st: RecencyStoreState): Unit =
    store.setState(st.store.asInstanceOf)
    addressTranslation = st.addrTrans
    mostRecent = st.mostRecent

//class RecencyAbstractly[Addr, Context](c: CAllocationIntIncrement[Context], addr: Context => Addr) extends Abstractly[(Context,Int), Addr]:
//  override def apply(caddr: (Context,Int)): Addr = addr(caddr._1)
