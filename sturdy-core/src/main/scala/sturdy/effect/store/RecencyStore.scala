package sturdy.effect.store

import sturdy.{IsSound, Soundness}
import sturdy.data.{JOption, JOptionA, WithJoin, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.{ComputationJoiner, Effect, EffectList, EffectListJoiner, Stateless, TrySturdy}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}

import scala.collection.immutable.{ArraySeq, BitSet, HashMap, IntMap}
import scala.collection.{MapView, mutable}
import scala.reflect.ClassTag
import scala.util.boundary
import boundary.break

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

  def joinRecentIntoOld(mapping: Map[Context, RecencyRegion], virt: Virt): Unit =
    virt.iterator.foreach(
      v =>
        val recency = addressTranslation.recency(mapping, v.ctx, v.n)
        if (recency == PowRecency.Recent || recency == PowRecency.RecentOld) {
          val (ctx, _) = v.identifier
          store.copy(
            PowersetAddr(PhysicalAddress(ctx, Recency.Recent)),
            PowersetAddr(PhysicalAddress(ctx, Recency.Old)))
          addressTranslation.joinRecentIntoOld(v)
        }
    )

  inline def joinRecentIntoOld(virt: Virt): Unit =
    joinRecentIntoOld(addressTranslation.mapping, virt)

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    store.addressIterator(valueIterator)

  def collectGarbage(alive: PowVirtualAddress[Context]): Unit =
    val deadPhysicals = addressTranslation.deadPhysicalAddresses(alive)
    store.free(deadPhysicals)
    addressTranslation.removePhysicalAddresses(deadPhysicals)

  override type State = initStore.State
  override inline def getState: State = this.store.getState
  override inline def setState(st: State): Unit = store.setState(st.asInstanceOf)
  override def setBottom: Unit = this.store.setBottom
  override def join: Join[State] = initStore.join
  override def widen: Widen[State] = initStore.widen
  override def stackWiden: StackWidening[State] = initStore.stackWiden

  def virtualAddresses: PowVirtualAddress[Context] =
    addressTranslation.virtualAddresses

  def virtualAddresses(ctx: Context): PowVirtualAddress[Context] =
    addressTranslation.virtualAddresses(ctx)

  def physicalAddressesByContext: Map[Context, PowPhysicalAddress[Context]] =
    addressTranslation.physicalAddressesByContext

  def virtualAddressesByContext: Map[Context, PowVirtualAddress[Context]] =
    addressTranslation.virtualAddressesByContext

  def isSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Context], vSoundness: Soundness[cV, V]): IsSound = boundary:
    val contextMap = physicalAddressesByContext
    c.entries.foreachEntry { case (a, v) =>
      val ctx = varAbstractly(a)
      val ps = contextMap.getOrElse(ctx, PowersetAddr(Set()))
      store.read(ps) match
        case JOptionA.None() => break(IsSound.NotSound(s"Concrete address $a abstracts to $ps, which is not bound in store"))
        case JOptionA.Some(av) =>
          val s = vSoundness.isSound(v, av)
          if (s.isNotSound) break(s)
        case JOptionA.NoneSome(av) =>
          val s = vSoundness.isSound(v, av)
          if (s.isNotSound) break(s)
    }
    IsSound.Sound

case class RecencyClosure[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V](recencyStore: RecencyStore[Context, Virt, V], effect: Effect) extends Effect:
  override type State = RecencyClosureState[Context, Virt, V, effect.State]

  override def getState: State =
    RecencyClosureState(recencyStore, recencyStore.addressTranslation.getState, recencyStore.getState, effect.getState)

  override def setState(st: State): Unit =
    recencyStore.addressTranslation.setState(st.addrTransState.asInstanceOf)
    recencyStore.setState(st.recencyStoreState.asInstanceOf)
    effect.setState(st.effectState)

  override def setBottom: Unit =
    recencyStore.addressTranslation.setBottom
    recencyStore.setBottom
    effect.setBottom

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    recencyStore.addressIterator(valueIterator) ++ effect.addressIterator(valueIterator)

  override def join: Join[State] = combine(recencyStore.addressTranslation.join, recencyStore.join, effect.join)
  override def widen: Widen[State] = combine(recencyStore.addressTranslation.widen, recencyStore.widen, effect.widen)
  def combine[W <: Widening](combineAddrTrans: Combine[recencyStore.addressTranslation.State, W], combineRecencyStore: Combine[recencyStore.State, W], combineState: Combine[effect.State, W]): Combine[State, W] =
    (v1: State, v2: State) =>
      if(v1 == v2) {
        // Performance optimization: Avoid joining if the states are equal.
        Unchanged(v1)
      } else {
        val addrTrans = recencyStore.addressTranslation
        val snapshotMapping = addrTrans.mapping
        val snapshotOtherMapping = addrTrans.otherMapping
        val snapshotStore = recencyStore.store.getState
        try {
          val joinedAddrTrans = combineAddrTrans(v1.addrTransState.asInstanceOf, v2.addrTransState.asInstanceOf)
          addrTrans.mapping = joinedAddrTrans.get.mapping
          addrTrans.otherMapping = None

          val joinedRecencyStore = combineRecencyStore(v1.recencyStoreState.asInstanceOf, v2.recencyStoreState.asInstanceOf)
          recencyStore.setState(joinedRecencyStore.get)

          // Joining the states v1 and v2 has the side effect of allocating new virtual addresses and mutate the recency store.
          // Hence, we need to join the current state of the address translation and recency store afterwards to avoid forgetting these addresses.
          val joinedEffectState = combineState(v1.effectState, v2.effectState)

          val newRecencyStore = recencyStore.getState

          MaybeChanged(
            RecencyClosureState(recencyStore, recencyStore.addressTranslation.getState, newRecencyStore, joinedEffectState.get),
            joinedRecencyStore.hasChanged || newRecencyStore != joinedRecencyStore.get || joinedEffectState.hasChanged
          )
        } finally {
          addrTrans.mapping = snapshotMapping
          addrTrans.otherMapping = snapshotOtherMapping
          recencyStore.store.setState(snapshotStore)
        }
      }


  override def stackWiden: StackWidening[State] =
    (stack: List[State], call: State) =>
      val addrTrans = recencyStore.addressTranslation
      val snapshotMapping = addrTrans.mapping
      val snapshotOtherMapping = addrTrans.otherMapping
      val snapshotStore = recencyStore.store.getState
      try {
        val initMapping = stack.foldLeft(call.addrTransState.asInstanceOf[addrTrans.State])((accum, state) => addrTrans.join(accum, state.addrTransState.asInstanceOf).get)
        addrTrans.mapping = initMapping.mapping
        addrTrans.otherMapping = None

        val joinedRecencyStore = recencyStore.stackWiden(stack.map(state => state.recencyStoreState.asInstanceOf[recencyStore.State]), call.recencyStoreState.asInstanceOf[recencyStore.State])
        recencyStore.setState(joinedRecencyStore.get)

        val joinedEffectState = effect.stackWiden(stack.map(state => state.effectState), call.effectState)

        val newRecencyStore = recencyStore.getState

        val newAddrs = addrTrans.getState.difference(initMapping)
        val newAddrTransState = recencyStore.addressTranslation.join(call.addrTransState.asInstanceOf, newAddrs.asInstanceOf).get

        MaybeChanged(
          RecencyClosureState(recencyStore, newAddrTransState, newRecencyStore, joinedEffectState.get),
          joinedRecencyStore.hasChanged || newRecencyStore != joinedRecencyStore || joinedEffectState.hasChanged
        )
      } finally {
        addrTrans.mapping = snapshotMapping
        addrTrans.otherMapping = snapshotOtherMapping
        recencyStore.store.setState(snapshotStore)
      }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = EffectList(recencyStore.addressTranslation, recencyStore, effect).makeComputationJoiner[A]

object RecencyClosure:
  def apply[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V](recencyStore: RecencyStore[Context, Virt, V]): RecencyClosure[Context, Virt, V] = new RecencyClosure(recencyStore, new Stateless {})

private final class RecencyClosureState[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V, EffectState](val recencyStore: RecencyStore[Context, Virt, V], val addrTransState: recencyStore.addressTranslation.State, val recencyStoreState: recencyStore.State, val effectState: EffectState):
  val addrTrans = recencyStore.addressTranslation

  override def equals(obj: Any): Boolean =
    obj match
      case other: RecencyClosureState[Context @unchecked, Virt @unchecked, V @unchecked, EffectState @unchecked]
        if this.hash == other.hash =>
          val snapshotMapping = addrTrans.mapping
          val snapshotOtherMapping = addrTrans.otherMapping
          try {
            addrTrans.mapping = this.addrTransState.mapping
            addrTrans.otherMapping = Some(other.addrTransState.mapping)
            this.addrTransState.equals(other.addrTransState) &&
              this.recencyStoreState.equals(other.recencyStoreState) &&
              this.effectState.equals(other.effectState)
          } finally {
            addrTrans.mapping = snapshotMapping
            addrTrans.otherMapping = snapshotOtherMapping
          }
      case _ => false

  lazy val hash = {
    val snapshotMapping = addrTrans.mapping
    try {
      addrTrans.mapping = this.addrTransState.mapping
      (addrTransState, recencyStoreState, effectState).hashCode()
    } finally {
      addrTrans.mapping = snapshotMapping
    }
  }
  override def hashCode(): Int = hash

  override def toString: String =
    f"RecencyClosureState(${hashCode()}, ${addrTransState}, ${recencyStoreState}, ${effectState})"