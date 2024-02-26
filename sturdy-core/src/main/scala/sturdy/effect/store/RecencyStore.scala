package sturdy.effect.store

import sturdy.{IsSound, Soundness}
import sturdy.data.{JOption, JOptionA, WithJoin, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.{ComputationJoiner, Stateless, TrySturdy}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}

import scala.collection.immutable.{HashMap, IntMap}
import scala.collection.{MapView, mutable}
import scala.reflect.ClassTag

final class RecencyStore[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V]
  (val initStore: Store[PowPhysicalAddress[Context], V, WithJoin],
   val addressTranslation: AddressTranslation[Context] = AddressTranslation.empty[Context])
  (using Finite[Context])
  extends Store[Virt, V, WithJoin], Allocator[VirtualAddress[Context], Context]:

  private val store: initStore.type = initStore
  protected var mostRecent: HashMap[Context, Powerset[Int]] = HashMap()
  protected var next: Int = 0
  def getNext() = { next += 1; next }

  def getAddressTranslation: AddressTranslation[Context] = this.addressTranslation

  private def virtToPhys(v: VirtualAddress[Context]): PowPhysicalAddress[Context] =
    v.physical

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

  override def free(vs: Virt): Unit = {}
//    val pa = virtToPhys(vs)
//    store.free(pa)
//    if (vs.isStrong)
//      vs.reduce(v => addressTranslation -= ((v.ctx,v.n)))

  def alloc(ctx: Context): VirtualAddress[Context] =
    val fresh = getNext()
    mostRecent.get(ctx) match
      case Some(mostRecentVirts) =>
        mostRecent += ctx -> Powerset(fresh)
        val virt = VirtualAddress(ctx, fresh, addressTranslation)
        addressTranslation += virt.identifier -> PowRecency.Recent
        store.move(
          PowersetAddr(PhysicalAddress(ctx, Recency.Recent)),
          PowersetAddr(PhysicalAddress(ctx, Recency.Old)))
        for(mostRecentVirt <- mostRecentVirts)
          addressTranslation += (ctx,mostRecentVirt) -> PowRecency.Old
        virt
      case None =>
        mostRecent += ctx -> Powerset(fresh)
        val virt = VirtualAddress(ctx, fresh, addressTranslation)
        addressTranslation += virt.identifier -> PowRecency.Recent
        virt

  def joinRecentIntoOld(virt: Virt) =
    virt.iterator.foreach(
      v =>
        val (ctx,_) = v.identifier
        store.copy(
          PowersetAddr(PhysicalAddress(ctx, Recency.Recent)),
          PowersetAddr(PhysicalAddress(ctx, Recency.Old)))
        addressTranslation += v.identifier -> PowRecency.Old
    )

  override type State = RecencyStoreState

  case class RecencyStoreState(store: initStore.State,
                               addrTrans: addressTranslation.State,
                               mostRecent: HashMap[Context, Powerset[Int]]):
    override def equals(obj: Any): Boolean =
      obj match
        case other: RecencyStoreState =>
          this.store.equals(other.store)
        case _ => false

    override def hashCode(): Int =
      store.hashCode()

  override def getState: RecencyStoreState =
    RecencyStoreState(this.store.getState.asInstanceOf, this.addressTranslation.getState, this.mostRecent)

  override inline def setState(st: RecencyStoreState): Unit =
    store.setState(st.store.asInstanceOf)
    addressTranslation.setState(st.addrTrans)
    mostRecent = mostRecent.merged(st.mostRecent) {
      case ((ctx1,recent1),(_,recent2)) => (ctx1, recent1 ++ recent2)
    }

  override def mapState(st: RecencyStoreState, f: [A] => A => A): RecencyStoreState =
    RecencyStoreState(initStore.mapState(st.store, f), st.addrTrans, st.mostRecent)

  override def join: Join[RecencyStoreState] = new CombineRecencyStoreState(initStore.join)
  override def widen: Widen[RecencyStoreState] = new CombineRecencyStoreState(initStore.widen)
  private class CombineRecencyStoreState[W <: Widening](combineStore: Combine[initStore.State, W]) extends Combine[RecencyStoreState, W]:
    override def apply(state1: RecencyStoreState, state2: RecencyStoreState): MaybeChanged[RecencyStoreState] =
      val combinedStores = combineStore(state1.store, state2.store)
      val combinedAddrTrans = Combine(state1.addrTrans, state2.addrTrans)
      val combinedMostRecent = Combine(state1.mostRecent, state2.mostRecent)
      MaybeChanged(RecencyStoreState(combinedStores.get, combinedAddrTrans.get, combinedMostRecent.get),
        hasChanged = combinedStores.hasChanged)

  def virtualAddresses: PowVirtualAddress[Context] =
    addressTranslation.virtualAddresses

  def virtualAddresses(ctx: Context): PowVirtualAddress[Context] =
    addressTranslation.virtualAddresses(ctx)

  def physicalAddressesByContext: Map[Context, PowPhysicalAddress[Context]] =
    addressTranslation.physicalAddressesByContext

  def virtualAddressesByContext: Map[Context, PowVirtualAddress[Context]] =
    addressTranslation.virtualAddressesByContext

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

