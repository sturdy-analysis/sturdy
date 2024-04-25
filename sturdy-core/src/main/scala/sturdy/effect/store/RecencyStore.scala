package sturdy.effect.store

import sturdy.{IsSound, Soundness}
import sturdy.data.{JOption, JOptionA, WithJoin, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.{ComputationJoiner, Effect, EffectListJoiner, Stateless, TrySturdy}
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

  val store: initStore.type = initStore

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

  case class RecencyStoreState(addrTrans: addressTranslation.State, store: initStore.State):
    override def equals(obj: Any): Boolean =
      obj match
        case other: RecencyStoreState =>
          this.store.equals(other.store)
    override def hashCode(): Int =
      this.store.hashCode()

  override def getState: RecencyStoreState =
    RecencyStoreState(this.addressTranslation.getState, this.store.getState.asInstanceOf)

  override inline def setState(st: RecencyStoreState): Unit =
    this.addressTranslation.setState(st.addrTrans)
    store.setState(st.store.asInstanceOf)

  override def join: Join[RecencyStoreState] = new CombineRecencyStoreState(addressTranslation.join, initStore.join)
  override def widen: Widen[RecencyStoreState] = new CombineRecencyStoreState(addressTranslation.widen, initStore.widen)
  private class CombineRecencyStoreState[W <: Widening](combineAddrTrans: Combine[addressTranslation.State, W], combineStore: Combine[initStore.State, W]) extends Combine[RecencyStoreState, W]:
    override def apply(state1: RecencyStoreState, state2: RecencyStoreState): MaybeChanged[RecencyStoreState] =
      val combinedAddrTrans = combineAddrTrans(state1.addrTrans, state2.addrTrans)
      val combinedStores = combineStore(state1.store, state2.store)
      MaybeChanged(RecencyStoreState(combinedAddrTrans.get, combinedStores.get),
        hasChanged = combinedAddrTrans.hasChanged || combinedStores.hasChanged)


  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(EffectListJoiner[A](List(addressTranslation, store)))

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

case class RecencyClosure[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V](recencyStore: RecencyStore[Context, Virt, V], effect: Effect) extends Effect:

  override type State = RecencyClosureState[Context, Virt, V, effect.State]
  override def getState: State =
    RecencyClosureState(recencyStore, recencyStore.getState, effect.getState)

  override def setState(st: State): Unit =
    recencyStore.setState(st.recencyStoreState.asInstanceOf)
    effect.setState(st.effectState)

  override def join: Join[State] = combine(recencyStore.join, effect.join)
  override def widen: Widen[State] = combine(recencyStore.widen, effect.widen)

  def combine[W <: Widening](combineRecencyStore: Combine[recencyStore.State, W], combineState: Combine[effect.State, W]): Combine[State, W] =
    (v1: State, v2: State) =>
      val addrTrans = recencyStore.addressTranslation
      val snapshotMapping = addrTrans.mapping
      val snapshotOtherMapping = addrTrans.otherMapping
      val snapshotStore = recencyStore.store.getState
      try {
        addrTrans.mapping = v1.recencyStoreState.addrTrans
        addrTrans.otherMapping = Some(v2.recencyStoreState.addrTrans)
        recencyStore.store.setState(v1.recencyStoreState.store.asInstanceOf)
        val j1 = combineState(v1.effectState, v2.effectState)
        val j2 = combineRecencyStore(recencyStore.getState, v2.recencyStoreState.asInstanceOf)
        MaybeChanged(RecencyClosureState(recencyStore, j2.get, j1.get), j1.hasChanged || j2.hasChanged)
      } finally {
        addrTrans.mapping = snapshotMapping
        addrTrans.otherMapping = snapshotOtherMapping
        recencyStore.store.setState(snapshotStore)
      }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(EffectListJoiner[A](List(recencyStore, effect)))

object RecencyClosure:
  def apply[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V](recencyStore: RecencyStore[Context, Virt, V]): RecencyClosure[Context, Virt, V] = new RecencyClosure(recencyStore, new Stateless {})

final class RecencyClosureState[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V, EffectState](val recencyStore: RecencyStore[Context, Virt, V], val recencyStoreState: recencyStore.State, val effectState: EffectState):
  val addrTrans = recencyStore.addressTranslation

  override def equals(obj: Any): Boolean =
    obj match
      case other: RecencyClosureState[Context @unchecked, Virt @unchecked, V @unchecked, EffectState @unchecked] =>
        val snapshotMapping = addrTrans.mapping
        val snapshotOtherMapping = addrTrans.otherMapping
        try {
          addrTrans.mapping = this.recencyStoreState.addrTrans
          addrTrans.otherMapping = Some(other.recencyStoreState.addrTrans)
          this.recencyStoreState.equals(other.recencyStoreState)
          this.effectState.equals(other.effectState)
        } finally {
          addrTrans.mapping = snapshotMapping
          addrTrans.otherMapping = snapshotOtherMapping
        }
      case _ => false

  override def hashCode(): Int =
    val snapshotMapping = addrTrans.mapping
    try {
      addrTrans.mapping = this.recencyStoreState.addrTrans
      (recencyStoreState, effectState).hashCode()
    } finally {
      addrTrans.mapping = snapshotMapping
    }

  override def toString: String =
    f"RecencyClosureState(${hashCode()}, ${recencyStoreState}, ${effectState})"