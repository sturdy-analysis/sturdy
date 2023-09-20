package sturdy.effect.store

import sturdy.{IsSound, Soundness}
import sturdy.data.{JOption, JOptionA, WithJoin, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.{ComputationJoiner, Stateless, TrySturdy}
import sturdy.values.references.{AbstractAddr, PowersetAddr, joinPowersetAddr}
import sturdy.values.{*, given}

import scala.collection.immutable.{HashMap, IntMap}
import scala.collection.{MapView, mutable}
import scala.reflect.ClassTag

class RecencyStore[Context, Virt <: AbstractAddr[VirtualAddress[Context]], V]
  (val initStore: Store[PowPhysicalAddress[Context], V, WithJoin])
  (using Join[V], Widen[V], Finite[Context])
  extends Store[Virt, V, WithJoin], Allocator[VirtualAddress[Context], Context]:

  private val store: initStore.type = initStore
  protected var addressTranslation: Map[(Context,Int), PowRecency] = Map()
  protected var mostRecent: Map[Context, Powerset[Int]] = HashMap()
  protected var next: Int = 0
  def getNext() = { next += 1; next }

  private def virtToPhys(v: VirtualAddress[Context]): PowPhysicalAddress[Context] =
    addressTranslation.get(v.ctx, v.n) match
      case None =>
        throw new IllegalStateException(s"Unbound virtual address $v")
      case Some(PowRecency.Recent) => PowersetAddr(PhysicalAddress(v.ctx, Recency.Recent))
      case Some(PowRecency.Old) => PowersetAddr(PhysicalAddress(v.ctx, Recency.Old))
      case Some(PowRecency.RecentOld) => PowersetAddr(PhysicalAddress(v.ctx, Recency.Recent), PhysicalAddress(v.ctx, Recency.Old))

  private def virtToPhys(vs: Virt): PowPhysicalAddress[Context] =
    if (vs.isEmpty)
      PowersetAddr(Set())
    else
      vs.reduce(virtToPhys)

  override def read(vs: Virt): JOption[WithJoin, V] =
    val pa = virtToPhys(vs)
    store.read(pa)

  def write(vs: Virt, v: V): Unit =
    val pa = virtToPhys(vs)
    store.write(pa, v)

  override def free(vs: Virt): Unit =
    val pa = virtToPhys(vs)
    store.free(pa)
    if (vs.isStrong)
      vs.reduce(v => addressTranslation -= ((v.ctx,v.n)))

  def alloc(ctx: Context): VirtualAddress[Context] =
    val fresh = getNext()
    mostRecent.get(ctx) match
      case Some(mostRecentVirts) =>
        mostRecent += ctx -> Powerset(fresh)
        val virt = VirtualAddress(ctx, fresh, virtToPhys)
        addressTranslation += virt.identifier -> PowRecency.Recent
        store.read(PowersetAddr(PhysicalAddress(ctx, Recency.Recent))).map { oldVal =>
          store.write(PowersetAddr(PhysicalAddress(ctx, Recency.Old)), oldVal)
        }
        for(mostRecentVirt <- mostRecentVirts)
          addressTranslation += (ctx,mostRecentVirt) -> PowRecency.Old
        virt
      case None =>
        mostRecent += ctx -> Powerset(fresh)
        val virt = VirtualAddress(ctx, fresh, virtToPhys)
        addressTranslation += virt.identifier -> PowRecency.Recent
        virt

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

  def virtualAddresses: PowersetAddr[VirtualAddress[Context], VirtualAddress[Context]] =
    PowersetAddr(addressTranslation.keySet.map((ctx, n) => VirtualAddress(ctx, n, virtToPhys)))

  def virtualAddresses(ctx: Context): PowersetAddr[VirtualAddress[Context], VirtualAddress[Context]] =
    PowersetAddr(addressTranslation.keySet.filter(_._1 == ctx).map((ctx, n) => VirtualAddress(ctx, n, virtToPhys)))

  def physicalAddressesByContext: Map[Context, PowPhysicalAddress[Context]] =
    val x = addressTranslation.groupMapReduce
      (_._1._1)
      { case ((ctx,_), PowRecency.Old) => PowersetAddr(PhysicalAddress(ctx, Recency.Old))
        case ((ctx,_), PowRecency.Recent) => PowersetAddr(PhysicalAddress(ctx, Recency.Recent))
        case ((ctx,_), PowRecency.RecentOld) => PowersetAddr(PhysicalAddress(ctx, Recency.Old), PhysicalAddress(ctx, Recency.Recent))
      }
      (Join.compute)
    x

  def virtualAddressesByContext: Map[Context, PowVirtualAddress[Context]] =
    val x = addressTranslation.groupMapReduce
      (_._1._1)
      {
        case ((ctx, n), _) => PowersetAddr(VirtualAddress(ctx, n, virtToPhys))
      }
      (Join.compute)
    x

  def isSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Context], vSoundness: Soundness[cV, V]): IsSound =
    val contextMap = physicalAddressesByContext
    c.entries.foreachEntry { case (a, v) =>
      val ctx = varAbstractly(a)
      val ps = contextMap.getOrElse(ctx, PowersetAddr(Set()))
      store.read(ps) match
        case JOptionA.None() => return IsSound.NotSound(s"Concrete address $a abstracts to $ps, which is not bound in store")
        case JOptionA.Some(av) =>
          val s = vSoundness.isSound(v, av)
          if (s.isNotSound) return s
        case JOptionA.NoneSome(av) =>
          val s = vSoundness.isSound(v, av)
          if (s.isNotSound) return s
    }
    IsSound.Sound

