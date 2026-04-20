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
  (using Finite[Context], HasAddressTranslationState[Context, store.State])
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

  def allocPure(ctx: Context, state0: State): (VirtualAddress[Context], State) =
    val state1 = store.movePure(
      PowersetAddr(PhysicalAddress(ctx, Recency.Recent)),
      PowersetAddr(PhysicalAddress(ctx, Recency.Old)), state0)
    addressTranslation.allocPure(ctx, state1)

  inline def joinRecentIntoOld(virt: Virt): Unit =
    store.modifyInternalState(joinRecentIntoOldPure(virt, _))

  def joinRecentIntoOldPure(virt: Virt, state0: State): State = {
    var state = state0
    virt.iterator.foreach(
      v =>
        val recency = addressTranslation.recency(v.ctx, v.n, state)
        if (recency == PowRecency.Recent || recency == PowRecency.RecentOld) {
          val (ctx, _) = v.identifier
          state = addressTranslation.joinRecentIntoOld(v, state)
          state = store.copyPure(
                    PowersetAddr(PhysicalAddress(ctx, Recency.Recent)),
                    PowersetAddr(PhysicalAddress(ctx, Recency.Old)),
                    state)
        }
    )
    state
  }

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    store.addressIterator(valueIterator)

  def collectGarbage(alive: PowVirtualAddress[Context]): Unit =
    Profiler.addTime("RecencyStore.free") {
      store.modifyInternalState(state =>
        val deadPhysicals = addressTranslation.deadPhysicalAddresses(alive, state)
        store.freePure(deadPhysicals, state)
      )
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

  override def getState: State = RecencyClosureState(recencyStore, recencyStore.getState, effect.getState)

  override def setState(st: State): Unit =
    recencyStore.setState(st.recencyStoreState.asInstanceOf)
    effect.setState(st.effectState)

  override def setStateNonMonotonically(st: State): Unit =
    recencyStore.setStateNonMonotonically(st.recencyStoreState.asInstanceOf)
    effect.setStateNonMonotonically(st.effectState)

  override def setBottom: Unit =
    recencyStore.setBottom
    effect.setBottom

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    recencyStore.addressIterator(valueIterator) ++ effect.addressIterator(valueIterator)

  override def join: Join[State] = (state1, state2) => combine[Unit,Widening.No](recencyStore.joinClosingOver(using effect.joinClosingOver))((unit,state1), (unit,state2)).map(_._2)
  override def widen: Widen[State] = (state1, state2) => combine[Unit,Widening.Yes](recencyStore.widenClosingOver(using effect.widenClosingOver))((unit,state1),(unit,state2)).map(_._2)
  override def joinClosingOver[Codom](using Join[Codom]): Join[(Codom, State)] = combine[Codom,Widening.No](recencyStore.joinClosingOver(using effect.joinClosingOver))
  override def widenClosingOver[Codom](using Widen[Codom]): Widen[(Codom, State)] = combine[Codom,Widening.Yes](recencyStore.widenClosingOver(using effect.widenClosingOver))
  override def stackWiden: StackWidening[State] = (stack: List[State], call: State) =>
    recencyStore.widenClosingOver[List[effect.State]](using { case (es, List(call)) =>
      effect.stackWiden(es,call).map(List(_))
    })(
      (stack.map(_.effectState), stack.head.recencyStoreState.asInstanceOf[recencyStore.State]),
      (List(call.effectState), call.recencyStoreState.asInstanceOf[recencyStore.State])
    ).map { case (List(joinedEffectState), joinedRecencyStoreState) =>
      RecencyClosureState(recencyStore, joinedRecencyStoreState, joinedEffectState)
    }

  def combine[Codom, W <: Widening](combineAll: Combine[((Codom, effect.State), recencyStore.State), W]): Combine[(Codom,State), W] =
    (v1: (Codom,State), v2: (Codom,State)) =>
      val (codom1,state1) = v1; val (codom2,state2) = v2
      combineAll(
        ((codom1,state1.effectState),state1.recencyStoreState.asInstanceOf[recencyStore.State]),
        ((codom2,state2.effectState),state2.recencyStoreState.asInstanceOf[recencyStore.State])
      ).map {
        case ((joinedResult,joinedEffectState), joinedRencencyStore) =>
          (joinedResult, RecencyClosureState(recencyStore, joinedRencencyStore, joinedEffectState))
      }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new EffectListJoiner[A](Seq(recencyStore, effect)))

object RecencyClosure:
  def apply[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V](recencyStore: RecencyStore[Context, Virt, V]): RecencyClosure[Context, Virt, V] = new RecencyClosure(recencyStore, new Stateless {})
  val DEBUG = System.getProperty("RECENCY_CLOSURE_DEBUG", "false").toBoolean

private final class RecencyClosureState[Context: Ordering, Virt <: AbstractAddr[VirtualAddress[Context]], V, EffectState](val recencyStore: RecencyStore[Context, Virt, V], val recencyStoreState: recencyStore.store.State, val effectState: EffectState):
  val store = recencyStore.store

  override def equals(obj: Any): Boolean =
    obj match
      case other: RecencyClosureState[Context, Virt, V, EffectState] @unchecked if this.hash == other.hash =>
        val snapshotInternalState = store.internalStateOption.getOrElse(store.nullState)
        val snapshotLeftState = store.leftState.getOrElse(store.nullState)
        val snapshotRightState = store.rightState.getOrElse(store.nullState)
          try {
            store.setInternalState(store.nullState)
            store.setLeftState(this.recencyStoreState.asInstanceOf[store.State])
            store.setRightState(other.recencyStoreState.asInstanceOf[store.State])

            this.recencyStoreState.equals(other.recencyStoreState) && this.effectState.equals(other.effectState)
          } finally {
            store.setInternalState(snapshotInternalState)
            store.setLeftState(snapshotLeftState)
            store.setRightState(snapshotRightState)
          }
      case _ => false

  override def hashCode(): Int = hash
  private lazy val hash = {
    val snapshotInternalState = store.internalStateOption.getOrElse(store.nullState)
    val snapshotLeftState = store.leftState.getOrElse(store.nullState)
    val snapshotRightState = store.rightState.getOrElse(store.nullState)
    try {
      store.setLeftState(store.nullState)
      store.setRightState(store.nullState)
      store.setInternalState(this.recencyStoreState.asInstanceOf[store.State])
      (recencyStoreState, effectState).hashCode()
    } finally {
      store.setInternalState(snapshotInternalState)
      store.setLeftState(snapshotLeftState)
      store.setRightState(snapshotRightState)
    }
  }

  override def toString: String =
    if(RecencyClosure.DEBUG) {
      val snapshotInternalState = store.internalStateOption.getOrElse(store.nullState)
      val snapshotLeftState = store.leftState.getOrElse(store.nullState)
      val snapshotRightState = store.rightState.getOrElse(store.nullState)
      try {
        store.setInternalState(this.recencyStoreState.asInstanceOf[store.State])
        store.setLeftState(store.nullState)
        store.setRightState(store.nullState)
        f"RecencyClosureState(${hashCode()}, ${recencyStoreState}, ${effectState})"
      } finally {
        store.setInternalState(snapshotInternalState)
        store.setLeftState(snapshotLeftState)
        store.setRightState(snapshotRightState)
      }
    } else {
      s"RecencyClosureState(${hashCode()})"
    }