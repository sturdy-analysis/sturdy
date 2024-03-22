package sturdy.effect.callframe

import apron.*
import sturdy.apron.{ApronCons, ApronExpr, ApronRecencyState, ApronState, ApronType, ApronVar, IntApronType, given}
import sturdy.data.{JOption, JOptionA, JOptionC, NoJoin, WithJoin, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.callframe.{ConcreteCallFrame, JoinableDecidableCallFrame, MutableCallFrame}
import sturdy.effect.store.{RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

import scala.collection.immutable.HashMap

trait RelationalCallFrame
  [
    Data,
    Var: Ordering,
    CallSite,
    Ctx: Ordering: Finite,
    Type: ApronType: Join: Widen,
    Val: Join: Widen
  ]
  (
    initData: Data,
    initVars: Iterable[(Var, Option[Val])],
    val localVariableAllocator: Allocator[Ctx, Var],
    val apronState: ApronRecencyState[Ctx, Type, Val]
  )
  extends MutableCallFrame[Data, Var, Val, CallSite, NoJoin]
     with DecidableCallFrame[Data, Var, Val, CallSite]:

  final type VirtAddr = VirtualAddress[Ctx]
  final type PhysAddr = PhysicalAddress[Ctx]
  final type PowPhysAddr = PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]]
  final type PowVirtAddr = PowVirtualAddress[Ctx]
  final type ApronExprVirtAddr = ApronExpr[VirtualAddress[Ctx],Type]
  final type ApronExprPhysAddr = ApronExpr[PhysicalAddress[Ctx],Type]

  def makeRelationalVal(expr: ApronExprVirtAddr): Val

  val addressCallFrame: JoinableDecidableCallFrame[Data, Var, VirtAddr, CallSite] =
    JoinableDecidableCallFrame(
      initData,
      Iterable.empty
    )
//
//  val recencyStore = apronState.recencyStore
//  val relationalStore = apronState.relationalStore

  override def data: Data = addressCallFrame.data

  def getAddressTranslation: AddressTranslation[Ctx] = apronState.recencyStore.getAddressTranslation

  def setVars(newVars: Iterable[(Var, Option[Val])]) =
    addressCallFrame.setVars(
      newVars.map((variable, _) =>
        val ctx = localVariableAllocator.alloc(variable)
        (variable, Some(apronState.recencyStore.alloc(ctx)))
      )
    )

    for((variable, exprOption) <- newVars;
        expr <- exprOption)
      setLocalByName(variable, expr)

  setVars(initVars)

  override def setLocal(idx: Int, v: Val): JOptionC[Unit] =
    addressCallFrame.getLocal(idx).map(virt =>
      apronState.recencyStore.write(PowVirtualAddress(virt), v)
    )

  override def setLocalByName(x: Var, v: Val): JOptionC[Unit] =
    addressCallFrame.getFrameNames.get(x) match
      case None => JOptionC.none
      case Some(idx) => setLocal(idx, v)

  override def getLocal(x: Int): JOptionC[Val] =
    addressCallFrame.getLocal(x) match
      case JOptionC.Some(virt) =>
        val v1 = for{
          tpe <- apronState.relationalStore.getType(virt.physical)
        } yield makeRelationalVal(ApronExpr.addr(virt, tpe))

        val v2 = apronState.relationalStore.nonRelationalStore.read(virt.physical)

        Join(v1,v2).get.toJOptionC

      case JOptionC.None() => JOptionC.none

  override def getLocalByName(x: Var): JOptionC[Val] =
    addressCallFrame.getLocalByName(x) match
      case JOptionC.Some(virt) =>

        val v1 = for {
          tpe <- apronState.relationalStore.getType(virt.physical)
        } yield makeRelationalVal(ApronExpr.addr(virt, tpe))

        val v2 = apronState.relationalStore.nonRelationalStore.read(virt.physical)

        Join(v1,v2).get.toJOptionC

      case JOptionC.None() => JOptionC.none

  override def withNew[A](d: Data, vars: Iterable[(Var, Option[Val])], site: CallSite)(f: => A): A =
    val virtAddrs = vars.map((variable, _) =>
      val ctx = localVariableAllocator.alloc(variable)
      (variable, Some(apronState.recencyStore.alloc(ctx)))
    )
    addressCallFrame.withNew(d, virtAddrs, site) {
      for ((variable, exprOption) <- vars; expr <- exprOption)
        setLocalByName(variable, expr)
      f
    }

  case class ApronCallFrameState(recencyStoreState: apronState.recencyStore.State, addressCallFrameState: addressCallFrame.State)


  override type State = ApronCallFrameState

  override def getState: State =
    ApronCallFrameState(apronState.recencyStore.getState, addressCallFrame.getState)

  override def setState(state: State): Unit =
    apronState.recencyStore.setState(state.recencyStoreState)
    addressCallFrame.setState(state.addressCallFrameState)

  override def mapState(state: State, f: [A] => A => A): State =
    ApronCallFrameState(
      apronState.recencyStore.mapState(state.recencyStoreState, f),
      addressCallFrame.mapState(state.addressCallFrameState, f)
    )

  override def join: Join[State] = combineApronCallFrameState(_, _, apronState.recencyStore.join)
  override def widen: Widen[State] = combineApronCallFrameState(_, _, apronState.recencyStore.widen)

  def combineApronCallFrameState[W <: Widening](v1: ApronCallFrameState, v2: ApronCallFrameState, combineRecencyStore: Combine[apronState.recencyStore.State, W]): MaybeChanged[ApronCallFrameState] =
      val joinedRecencyStoreState = combineRecencyStore(v1.recencyStoreState, v2.recencyStoreState)

      val backupState = apronState.recencyStore.getState

      try {
        apronState.recencyStore.setState(joinedRecencyStoreState.get)

        if (v1.addressCallFrameState.length != v2.addressCallFrameState.length) {
          throw new IllegalStateException(s"Cannot join call frames ${v1} and ${v2} of equal size")
        } else {
          val joinedAddressCallFrameState = v1.addressCallFrameState.zip(v2.addressCallFrameState).map((virt1, virt2) =>
            for(tpe <- apronState.relationalStore.getType(virt2.physical).toOption) {
              val sourceExpr = ApronExpr.Addr(virt2, tpe)
              apronState.recencyStore.write(PowVirtualAddress(virt1), makeRelationalVal(sourceExpr))
            }
            virt1
          )

          val updatedRecencyStoreState =
            combineRecencyStore(joinedRecencyStoreState.get, apronState.recencyStore.getState)

          MaybeChanged(
            ApronCallFrameState(
              updatedRecencyStoreState.get,
              joinedAddressCallFrameState
            ),
            joinedRecencyStoreState.hasChanged || updatedRecencyStoreState.hasChanged
          )
        }
      } finally {
        apronState.recencyStore.setState(backupState)
      }

  given VirtAddrJoin: Join[VirtAddr] with
    override def apply(v1: VirtAddr, v2: VirtAddr): MaybeChanged[VirtAddr] =
      throw UnsupportedOperationException("Virtual Addresses cannot be joined directly. Instead, they are joined inside of the apronStore.")

object RelationalCallFrame:
//  def apply[
//    Data,
//    Var: Ordering,
//    CallSite,
//    Ctx: Ordering : Finite,
//    Type: ApronType : Join : Widen,
//    Val: Join: Widen
//  ]
//  (
//    initData: Data,
//    initVars: Iterable[(Var, Option[Val])],
//    _makeRelationalVal: ApronExpr[VirtualAddress[Ctx], Type] => Val,
//    _getRelationalValIntern: Val => Option[ApronExpr[PhysicalAddress[Ctx], Type]],
//    _makeRelationalValIntern: (RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], Val], ApronExpr[PhysicalAddress[Ctx], Type]) => Val
//  )
//  (using
//    temporaryVariableAllocator: Allocator[Ctx, Type],
//    localVariableAllocator: Allocator[Ctx, Var],
//    apronManager: Manager
//  ): (RelationalCallFrame[Data, Var, CallSite, Ctx, Type, Val], ApronRecencyState[Ctx, Type, Val]) =
//
//    val (recencyStore,relationalStore) = RecencyRelationalStore[Ctx,Type, Val](_getRelationalValIntern, _makeRelationalValIntern)
//    given state: ApronRecencyState[Ctx, Type, Val] = new ApronRecencyState(temporaryVariableAllocator, recencyStore, relationalStore) {}
//    val callFrame = new RelationalCallFrame[Data, Var, CallSite, Ctx, Type, Val](initData, initVars, localVariableAllocator, state):
//      override def makeRelationalVal(expr: ApronExprVirtAddr): Val = _makeRelationalVal(expr)
//    (callFrame,state)

  def apply[
    Data,
    Var: Ordering,
    CallSite,
    Ctx: Ordering : Finite,
    Type: ApronType : Join : Widen
  ](
     initData: Data,
     initVars: Iterable[(Var, Option[ApronExpr[VirtualAddress[Ctx], Type]])]
   )(using
     temporaryVariableAllocator: Allocator[Ctx, Type],
     localVariableAllocator: Allocator[Ctx, Var],
     apronManager: Manager
   ): (RelationalCallFrame[Data, Var, CallSite, Ctx, Type, ApronExpr[VirtualAddress[Ctx], Type]], ApronRecencyState[Ctx, Type, ApronExpr[VirtualAddress[Ctx], Type]]) =
    val (recencyStore,relationalStore) = RecencyRelationalStore[Ctx,Type]
    given state: ApronRecencyState[Ctx, Type, ApronExpr[VirtualAddress[Ctx], Type]] = new ApronRecencyState[Ctx, Type, ApronExpr[VirtualAddress[Ctx], Type]](temporaryVariableAllocator, recencyStore, relationalStore)
    val callFrame = new RelationalCallFrame[Data, Var, CallSite, Ctx, Type, ApronExpr[VirtualAddress[Ctx], Type]](initData, initVars, localVariableAllocator, state):
      override def makeRelationalVal(expr: ApronExprVirtAddr): ApronExpr[VirtualAddress[Ctx], Type] = expr

    (callFrame, state)