package sturdy.effect.store

import sturdy.{IsSound, Soundness}
import sturdy.data.{JOption, JOptionA, WithJoin, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.{ComputationJoiner, Effect, EffectList, EffectListJoiner, Stateless, TrySturdy}
import sturdy.util.Profiler
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}

import scala.collection.immutable.{ArraySeq, BitSet, HashMap, IntMap}
import scala.collection.{MapView, mutable}
import scala.reflect.ClassTag
import scala.util.boundary
import boundary.break

class RecencyStore[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V]
  (val store: StoreWithPureOps[PowPhysicalAddress[Context], V, WithJoin] & AddressTranslation[Context])
  (using Finite[Context])
  extends Store[Virt, V, WithJoin], Allocator[VirtualAddress[Context], Context]:

  def addressTranslation: AddressTranslation[Context] = store

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

  override def free(vs: Virt): Unit =
    val pa = virtToPhys(vs)
    store.free(pa)

  def alloc(ctx: Context): VirtualAddress[Context] =
    store.move(
      PowersetAddr(PhysicalAddress(ctx, Recency.Recent)),
      PowersetAddr(PhysicalAddress(ctx, Recency.Old)))
    addressTranslation.alloc(ctx)

  inline def joinRecentIntoOld(virt: Virt): Unit =
    addressTranslation.modifyInternalAddressTranslationState(addrTransState =>
      store.withInternalState(state =>
        joinRecentIntoOldPure((addrTransState, state), virt)))

  def joinRecentIntoOldPure(state0: (AddressTranslationState[Context], State), virt: Virt): (AddressTranslationState[Context],State) = {
    var state = state0
    virt.iterator.foreach(
      v =>
        val recency = addressTranslation.recency(v.ctx, v.n, state._1)
        if (recency == PowRecency.Recent || recency == PowRecency.RecentOld) {
          val (ctx, _) = v.identifier
          state =
            (
              addressTranslation.joinRecentIntoOld(v, state._1),
              store.copyPure(
                PowersetAddr(PhysicalAddress(ctx, Recency.Recent)),
                PowersetAddr(PhysicalAddress(ctx, Recency.Old)),
                state._2)
            )
        }
    )
    state
  }

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    store.addressIterator(valueIterator)

  def collectGarbage(alive: PowVirtualAddress[Context]): Unit =
    Profiler.addTime("RecencyStore.free") {
      val deadPhysicals = addressTranslation.deadPhysicalAddresses(alive)
      store.free(deadPhysicals)
    }

  inline def withInternalState[A](f: State => (A,State)): A =
    store.withInternalState(f)

  override final type State = store.State
  override inline def getState: State = store.getState
  override inline def setState(st: State): Unit = store.setState(st)
  override inline def setStateNonMonotonically(st: State): Unit = store.setStateNonMonotonically(st)
  override inline def setBottom: Unit = store.setBottom
  override inline def join: Join[State] = store.join
  override inline def widen: Widen[State] = store.widen
  override inline def joinClosingOver[Body](using Join[Body]): Join[(Body, State)] = store.joinClosingOver
  override inline def widenClosingOver[Body](using Widen[Body]): Widen[(Body, State)] = store.widenClosingOver
  override inline def stackWiden: StackWidening[State] = store.stackWiden
  override inline def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = store.makeComputationJoiner

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

  override def getState: State = ???
//    RecencyClosureState(recencyStore, recencyStore.addressTranslation.getState, recencyStore.getState, effect.getState)

  override def setState(st: State): Unit =
    recencyStore.addressTranslation.setState(st.addrTransState.asInstanceOf)
    recencyStore.setState(st.recencyStoreState.asInstanceOf)
    effect.setState(st.effectState)

  override def setStateNonMonotonically(st: State): Unit =
    recencyStore.addressTranslation.setStateNonMonotonically(st.addrTransState.asInstanceOf)
    recencyStore.setStateNonMonotonically(st.recencyStoreState.asInstanceOf)
    effect.setStateNonMonotonically(st.effectState)

  override def setBottom: Unit =
    recencyStore.addressTranslation.setBottom
    recencyStore.setBottom
    effect.setBottom

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    recencyStore.addressIterator(valueIterator) ++ effect.addressIterator(valueIterator)

  override def join: Join[State] = ??? // (state1, state2) => combine[Unit,Widening.No](recencyStore.addressTranslation.join, recencyStore.joinClosingOver(using effect.joinClosingOver))((unit,state1), (unit,state2)).map(_._2)
  override def widen: Widen[State] = ??? // (state1, state2) => combine[Unit,Widening.Yes](recencyStore.addressTranslation.widen, recencyStore.widenClosingOver(using effect.widenClosingOver))((unit,state1),(unit,state2)).map(_._2)
  override def joinClosingOver[Codom](using Join[Codom]): Join[(Codom, State)] = ??? // combine[Codom,Widening.No](recencyStore.addressTranslation.join, recencyStore.joinClosingOver(using effect.joinClosingOver))
  override def widenClosingOver[Codom](using Widen[Codom]): Widen[(Codom, State)] = ??? // combine[Codom,Widening.Yes](recencyStore.addressTranslation.widen, recencyStore.widenClosingOver(using effect.widenClosingOver))

//  def combine[Codom, W <: Widening](combineAddrTrans: Combine[recencyStore.addressTranslation.State, W], combineAll: Combine[((Codom, effect.State), recencyStore.State), W]): Combine[(Codom,State), W] =
//    (v1: (Codom,State), v2: (Codom,State)) =>
//      if((v1._1 == v2._1) && (v1._2 eq v2._2)) {
//        // Performance optimization: Avoid joining if the states are equal.
//        Unchanged(v1)
//      } else {
//        val (codom1,state1) = v1; val (codom2,state2) = v2
//        val addrTrans = recencyStore.addressTranslation
//        val snapshotAddrTransInternalState = addrTrans.internalAddressTranslationState
//        val snapshotAddrTransLeftState = addrTrans.leftAddressTranslationState
//        val snapshotAddrTransRightState = addrTrans.rightAddressTranslationState
//        try {
//          addrTrans._leftState = state1.addrTransState
//          addrTrans._rightState = state2.addrTransState
//          val joinedAddrTrans = combineAddrTrans(addrTrans._leftState, addrTrans._rightState)
//          addrTrans._internalState = joinedAddrTrans.get
//
//          combineAll(
//            ((codom1,state1.effectState),state1.recencyStoreState.asInstanceOf[recencyStore.State]),
//            ((codom2,state2.effectState),state2.recencyStoreState.asInstanceOf[recencyStore.State])
//          ).map {
//            case ((joinedResult,joinedEffectState), joinedRencencyStore) =>
//              (joinedResult, RecencyClosureState(recencyStore, recencyStore.addressTranslation._internalState, joinedRencencyStore, joinedEffectState))
//          }
//        } finally {
//          addrTrans._internalState = snapshotAddrTransInternalState
//          addrTrans._leftState = snapshotAddrTransLeftState
//          addrTrans._rightState = snapshotAddrTransRightState
//        }
//      }


  override def stackWiden: StackWidening[State] = ???
//    (stack: List[State], call: State) =>
//      val addrTrans = recencyStore.addressTranslation
//      val snapshotAddrTransInternalState = addrTrans._internalState
//      val snapshotAddrTransLeftState = addrTrans._leftState
//      val snapshotAddrTransRightState = addrTrans._rightState
//      val snapshotStore = recencyStore.store.getState
//      try {
//        addrTrans._internalState = stack.foldLeft(call.addrTransState.asInstanceOf[addrTrans.State])((accum, state) => addrTrans.join(accum, state.addrTransState.asInstanceOf).get)
//        addrTrans._leftState = null
//        addrTrans._rightState = null
//
//        val stackRecencyStoreStates = stack.map(state => state.recencyStoreState.asInstanceOf[recencyStore.State])
//        var joinedRecencyStore = recencyStore.stackWiden(stackRecencyStoreStates, call.recencyStoreState.asInstanceOf[recencyStore.State])
//        recencyStore.setStateNonMonotonically(joinedRecencyStore.get)
//
//        val joinedEffectState = effect.stackWiden(stack.map(state => state.effectState), call.effectState)
//        joinedRecencyStore = joinedRecencyStore.flatMap(joinedStore => recencyStore.stackWiden(joinedStore :: stackRecencyStoreStates, recencyStore.getState))
//
//        val newAddrTransState = recencyStore.addressTranslation.getState
//
//        MaybeChanged(
//          RecencyClosureState(recencyStore, newAddrTransState, joinedRecencyStore.get, joinedEffectState.get),
//          joinedRecencyStore.hasChanged || joinedEffectState.hasChanged
//        )
//      } finally {
//        addrTrans._internalState = snapshotAddrTransInternalState
//        addrTrans._leftState = snapshotAddrTransLeftState
//        addrTrans._rightState = snapshotAddrTransRightState
//        recencyStore.store.setStateNonMonotonically(snapshotStore)
//      }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new EffectListJoiner[A](Seq(recencyStore.addressTranslation, recencyStore, effect)))

object RecencyClosure:
  def apply[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V](recencyStore: RecencyStore[Context, Virt, V]): RecencyClosure[Context, Virt, V] = new RecencyClosure(recencyStore, new Stateless {})

private final class RecencyClosureState[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V, EffectState](val recencyStore: RecencyStore[Context, Virt, V], val addrTransState: recencyStore.store.State, val recencyStoreState: recencyStore.State, val effectState: EffectState):
  val addrTrans = recencyStore.addressTranslation

  override def equals(obj: Any): Boolean = ???
//    obj match
//      case other: RecencyClosureState[Context @unchecked, Virt @unchecked, V @unchecked, EffectState @unchecked]
//        if this.hash == other.hash =>
//        val snapshotAddrTransInternalState = addrTrans._internalState
//        val snapshotAddrTransLeftState = addrTrans._leftState
//        val snapshotAddrTransRightState = addrTrans._rightState
//          try {
//            addrTrans._leftState = this.addrTransState
//            addrTrans._rightState = other.addrTransState
//            this.addrTransState.equals(other.addrTransState) &&
//              this.recencyStoreState.equals(other.recencyStoreState) &&
//              this.effectState.equals(other.effectState)
//          } finally {
//            addrTrans._internalState = snapshotAddrTransInternalState
//            addrTrans._leftState = snapshotAddrTransLeftState
//            addrTrans._rightState = snapshotAddrTransRightState
//          }
//      case _ => false

  lazy val hash = ???
//    {
//    val snapshotInternalState = addrTrans._internalState
//    try {
//      addrTrans._internalState = this.addrTransState
//      (addrTransState, recencyStoreState, effectState).hashCode()
//    } finally {
//      addrTrans._internalState = snapshotInternalState
//    }
//  }
  override def hashCode(): Int = hash

  override def toString: String = ???
//    val snapshotInternalState = addrTrans._internalState
//    try {
//      addrTrans._internalState = this.addrTransState
//      f"RecencyClosureState(${hashCode()}, ${addrTransState}, ${recencyStoreState}, ${effectState})"
//    } finally {
//      addrTrans._internalState = snapshotInternalState
//    }