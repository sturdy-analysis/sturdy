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

class RecencyStore[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V]
  (val initStore: Store[PowPhysicalAddress[Context], V, WithJoin],
   val addressTranslation: AddressTranslation[Context] = AddressTranslation.empty[Context])
  (using Finite[Context])
  extends Store[Virt, V, WithJoin], Allocator[VirtualAddress[Context], Context]:

  private val store: initStore.type = initStore

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
    store.move(
      PowersetAddr(PhysicalAddress(ctx, Recency.Recent)),
      PowersetAddr(PhysicalAddress(ctx, Recency.Old)))
    addressTranslation.alloc(ctx)

  def joinRecentIntoOld(virt: Virt) =
    virt.iterator.foreach(
      v =>
        if(v.recency == PowRecency.Recent || v.recency == PowRecency.RecentOld) {
          val (ctx, _) = v.identifier
          store.copy(
            PowersetAddr(PhysicalAddress(ctx, Recency.Recent)),
            PowersetAddr(PhysicalAddress(ctx, Recency.Old)))
          addressTranslation.joinRecentIntoOld(v)
        }
    )

  override type State = RecencyStoreState

  case class RecencyStoreState(store: initStore.State):
    override def equals(obj: Any): Boolean =
      obj match
        case other: RecencyStoreState =>
          this.store.equals(other.store)
        case _ => false

    override def hashCode(): Int =
      store.hashCode()

  override def getState: RecencyStoreState =
    RecencyStoreState(this.store.getState.asInstanceOf)

  override inline def setState(st: RecencyStoreState): Unit =
    store.setState(st.store.asInstanceOf)

  override def mapState(st: RecencyStoreState, f: [A] => A => A): RecencyStoreState =
    RecencyStoreState(initStore.mapState(st.store, f))

  override def join: Join[RecencyStoreState] = new CombineRecencyStoreState(initStore.join)
  override def widen: Widen[RecencyStoreState] = new CombineRecencyStoreState(initStore.widen)
  private class CombineRecencyStoreState[W <: Widening](combineStore: Combine[initStore.State, W]) extends Combine[RecencyStoreState, W]:
    override def apply(state1: RecencyStoreState, state2: RecencyStoreState): MaybeChanged[RecencyStoreState] =
      val combinedStores = combineStore(state1.store, state2.store)
      MaybeChanged(RecencyStoreState(combinedStores.get),
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

