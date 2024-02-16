package sturdy.effect.callframe

import apron.*
import sturdy.apron.{ApronCons, ApronExpr, ApronRecencyState, ApronState, ApronType, ApronVar, IntApronType, given}
import sturdy.data.{JOption, JOptionA, JOptionC, NoJoin, WithJoin}
import sturdy.effect.allocation.Allocator
import sturdy.effect.callframe.{ConcreteCallFrame, JoinableDecidableCallFrame, MutableCallFrame}
import sturdy.effect.store.{ApronRecencyStore, ApronStore, ClosedEquality, ClosedHashCode, RecencyStore, given}
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

import scala.collection.immutable.HashMap

final class ApronCallFrame
  [
    Data,
    Var: Ordering,
    CallSite,
    Ctx: Ordering: Finite,
    Type: ApronType: Join: Widen
  ]
  (
    initData: Data,
    initVars: Iterable[(Var, Option[ApronExpr[VirtualAddress[Ctx],Type]])],
    val recencyStore: RecencyStore[Ctx, PowVirtualAddress[Ctx], ApronExpr[PhysicalAddress[Ctx],Type]],
    val apronStore: ApronStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], ApronExpr[PhysicalAddress[Ctx],Type]]
  )
  (using
   temporaryVariableAllocator: Allocator[Ctx, Type],
   localVariableAllocator: Allocator[Ctx, Var],
    apronManager: Manager
  )
  extends MutableCallFrame[Data, Var, ApronExpr[VirtualAddress[Ctx],Type], CallSite, NoJoin]
     with DecidableCallFrame[Data, Var, ApronExpr[VirtualAddress[Ctx],Type], CallSite]
     with ApronRecencyState[Ctx, Type](temporaryVariableAllocator, recencyStore, apronStore):

  final type VirtAddr = VirtualAddress[Ctx]
  final type PhysAddr = PhysicalAddress[Ctx]
  final type PowPhysAddr = PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]]
  final type PowVirtAddr = PowVirtualAddress[Ctx]
  final type ApronExprVirtAddr = ApronExpr[VirtualAddress[Ctx],Type]
  final type ApronExprPhysAddr = ApronExpr[PhysicalAddress[Ctx],Type]

  val addressCallFrame: JoinableDecidableCallFrame[Data, Var, VirtAddr, CallSite] =
    JoinableDecidableCallFrame(
      initData,
      Iterable.empty
    )

  override def data: Data = addressCallFrame.data

  def getAddressTranslation: AddressTranslation[Ctx] = recencyStore.getAddressTranslation

  def setVars(newVars: Iterable[(Var, Option[ApronExprVirtAddr])]) =
    addressCallFrame.setVars(
      newVars.map((variable, _) =>
        val ctx = localVariableAllocator.alloc(variable)
        (variable, Some(recencyStore.alloc(ctx)))
      )
    )

    for((variable, exprOption) <- newVars;
        expr <- exprOption)
      setLocalByName(variable, expr)

  setVars(initVars)

  override def setLocal(idx: Int, exprVirtAddr: ApronExprVirtAddr): JOptionC[Unit] =
    addressCallFrame.getLocal(idx).map(virt =>
      recencyStore.write(PowVirtualAddress(virt), virtToPhys(exprVirtAddr))
    )

  override def setLocalByName(x: Var, expr: ApronExprVirtAddr): JOptionC[Unit] =
    addressCallFrame.getFrameNames.get(x) match
      case None => JOptionC.none
      case Some(idx) => setLocal(idx, expr)

  override def getLocal(x: Int): JOptionC[ApronExprVirtAddr] =
    addressCallFrame.getLocal(x).map(virt =>
      ApronExpr.addr(virt, apronStore.getType(virt.physical)))

  override def getLocalByName(x: Var): JOptionC[ApronExprVirtAddr] =
    addressCallFrame.getLocalByName(x).map(virt =>
      ApronExpr.addr(virt, apronStore.getType(virt.physical)))

  override def withNew[A](d: Data, vars: Iterable[(Var, Option[ApronExprVirtAddr])], site: CallSite)(f: => A): A =
    val virtAddrs = vars.map((variable, _) =>
      val ctx = localVariableAllocator.alloc(variable)
      (variable, Some(recencyStore.alloc(ctx)))
    )
    addressCallFrame.withNew(d, virtAddrs, site) {
      for ((variable, exprOption) <- vars; expr <- exprOption)
        setLocalByName(variable, expr)
      f
    }

  case class ApronCallFrameState(recencyStoreState: recencyStore.State, addressCallFrameState: addressCallFrame.State):
    override def equals(obj: Any): Boolean = throw new UnsupportedOperationException("Use ApronCallFrame.closedEquality")
    override def hashCode(): Int = throw new UnsupportedOperationException("Use ApronCallFrame.closedEquality")


  override type State = ApronCallFrameState

  override def getState: ApronCallFrameState =
    ApronCallFrameState(recencyStore.getState, addressCallFrame.getState)

  override def setState(state: ApronCallFrameState): Unit =
    recencyStore.setState(state.recencyStoreState)
    addressCallFrame.setState(state.addressCallFrameState)

  override def join: Join[ApronCallFrameState] =
    combineApronCallFrameStates(_, _, recencyStore.join)

  override def widen: Widen[ApronCallFrameState] =
    combineApronCallFrameStates(_, _, recencyStore.widen)

  def combineApronCallFrameStates[W <: Widening](state1: ApronCallFrameState, state2: ApronCallFrameState, combineRecencyStore: Combine[recencyStore.State, W]): MaybeChanged[ApronCallFrameState] =
    val joinedRecencyStoreState = combineRecencyStore(state1.recencyStoreState, state2.recencyStoreState)

    val backupState = recencyStore.getState

    try {
      recencyStore.setState(joinedRecencyStoreState.get)

      if (state1.addressCallFrameState.length != state2.addressCallFrameState.length) {
        throw new IllegalStateException(s"Cannot join call frames ${state1} and ${state2} of equal size")
      } else {
        val joinedAddressCallFrameState = state1.addressCallFrameState.zip(state2.addressCallFrameState).map((virt1, virt2) =>
          val sourceExpr = ApronExpr.Addr(virt2, apronStore.getType(virt2.physical))
          recencyStore.write(PowVirtualAddress(virt1), virtToPhys(sourceExpr))
          virt1
        )

        val updatedRecencyStoreState = combineRecencyStore(joinedRecencyStoreState.get, recencyStore.getState)

        MaybeChanged(
          ApronCallFrameState(
            updatedRecencyStoreState.get,
            joinedAddressCallFrameState
          ),
          joinedRecencyStoreState.hasChanged || updatedRecencyStoreState.hasChanged
        )
      }
    } finally {
      recencyStore.setState(backupState)
    }

  def closedEquality: ClosedEquality[recencyStore.addressTranslation.State, ApronCallFrameState] =
    new ClosedEquality[recencyStore.addressTranslation.State, ApronCallFrameState]:
      val recencyStoreEquals = recencyStore.closedEquality

      override def closedEquals(closure1: recencyStore.addressTranslation.State, state1: ApronCallFrameState, closure2: recencyStore.addressTranslation.State, state2: ApronCallFrameState): Boolean =
        recencyStoreEquals.closedEquals(closure1, state1.recencyStoreState, closure2, state2.recencyStoreState) &&
          ClosedEquality(closure1, state1.addressCallFrameState, closure2, state2.addressCallFrameState)

      override def closedHashCode(closure: recencyStore.addressTranslation.State, state: ApronCallFrameState): Int =
        (recencyStoreEquals.closedHashCode(closure, state.recencyStoreState),
          ClosedHashCode(closure, state.addressCallFrameState)).hashCode()

  given VirtAddrJoin: Join[VirtAddr] with
    override def apply(v1: VirtAddr, v2: VirtAddr): MaybeChanged[VirtAddr] =
      throw UnsupportedOperationException("Virtual Addresses cannot be joined directly. Instead, they are joined inside of the apronStore.")

object ApronCallFrame:
  def apply[
    Data,
    Var: Ordering,
    CallSite,
    Ctx: Ordering : Finite,
    Type: ApronType : Join : Widen
  ](
      initData: Data,
      initVars: Iterable[(Var, Option[ApronExpr[VirtualAddress[Ctx], Type]])],
  )(using
    temporaryVariableAllocator: Allocator[Ctx, Type],
    localVariableAllocator: Allocator[Ctx, Var],
    apronManager: Manager
  ): ApronCallFrame[Data, Var, CallSite, Ctx, Type] =

    val (recencyStore,apronStore) = ApronRecencyStore[Ctx,Type](apronManager)
    new ApronCallFrame(initData, initVars, recencyStore, apronStore)