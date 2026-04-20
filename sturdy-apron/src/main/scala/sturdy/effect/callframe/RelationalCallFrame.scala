package sturdy.effect.callframe

import apron.*
import sturdy.{IsSound, Soundness, seqIsSound}
import sturdy.apron.{ApronCons, ApronExpr, ApronRecencyState, ApronState, ApronType, ApronVar, IntApronType, StatelessRelationalExpr, given}
import sturdy.data.{JOption, JOptionA, JOptionC, MapEquals, NoJoin, WithJoin, given}
import sturdy.effect.{ComputationJoiner, EffectStack, TrySturdy}
import sturdy.effect.allocation.Allocator
import sturdy.effect.callframe.{ConcreteCallFrame, JoinableDecidableCallFrame, MutableCallFrame}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, WithWideningThresholds, given}
import sturdy.fix.DomLogger
import sturdy.util.{Lazy, lazily}
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

import scala.collection.immutable.{ArraySeq, HashMap}
import scala.reflect.ClassTag

final class RelationalCallFrame
  [
    Data,
    Var: Ordering,
    Val: Join: Widen,
    CallSite,
    Ctx: Ordering : Finite,
    Type: ApronType : Join : Widen,
  ]
  (
    initData: Data,
    initVars: Iterable[(Var, Option[Val])],
    val localVariableAllocator: Allocator[Ctx, (Var,Data)],
    val apronState: ApronRecencyState[Ctx, Type, Val],
    ssa: Boolean = false
  )(using
    relationalValue: StatelessRelationalExpr[Val, VirtualAddress[Ctx], Type]
  )
  extends MutableCallFrame[Data, Var, Val, CallSite, NoJoin]
     with DecidableCallFrame[Data, Var, Val, CallSite]:

  final type VirtAddr = VirtualAddress[Ctx]
  final type PhysAddr = PhysicalAddress[Ctx]
  final type PowPhysAddr = PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]]
  final type PowVirtAddr = PowVirtualAddress[Ctx]
  final type ApronExprVirtAddr = ApronExpr[VirtualAddress[Ctx],Type]
  final type ApronExprPhysAddr = ApronExpr[PhysicalAddress[Ctx],Type]

  val addressCallFrame: JoinableDecidableCallFrame[Data, Var, PowVirtAddr, CallSite] =
    JoinableDecidableCallFrame(
      initData,
      Iterable.empty
    )

  override def data: Data = addressCallFrame.data

  def getAddressTranslation: AddressTranslation[Ctx] = apronState.recencyStore.addressTranslation

  def setVars(newVars: Iterable[(Var, Option[Val])]) =
    addressCallFrame.setVars(
      newVars.map((variable, _) =>
        val ctx = localVariableAllocator.alloc((variable, addressCallFrame.data))
        (variable, Some(PowVirtualAddress(apronState.recencyStore.alloc(ctx))))
      )
    )

    for((variable, exprOption) <- newVars;
        expr <- exprOption)
      setLocalByName(variable, expr)

  setVars(initVars)

  def getVars: ArraySeq[Val] =
    ArraySeq.from(addressCallFrame.getVars).map {
      virt =>
        getByVirt(virt).getOrElse(
          throw new IllegalStateException(s"Virtual Address $virt not bound in call frame ${this.getState}")
        )
    }


  override def setLocal(idx: Int, v: Val): JOptionC[Unit] =
    addressCallFrame.getLocal(idx).map(_virts =>
      val name = findName(idx)
//      if(ssa) {
//        for(virts <- addressCallFrame.getLocal(idx).toOption) {
//          apronState.recencyStore.write(virts, v)
//        }
//      } else {
        val ctx = localVariableAllocator.alloc((name, addressCallFrame.data))
        val freshVirt = PowVirtualAddress(apronState.recencyStore.alloc(ctx))
        addressCallFrame.setLocal(idx, freshVirt)
        apronState.recencyStore.write(freshVirt, v)
//      }
    )

  override def setLocalByName(x: Var, v: Val): JOptionC[Unit] =
    addressCallFrame.getFrameNames.get(x) match
      case None => JOptionC.none
      case Some(idx) => setLocal(idx, v)

  override def getLocal(x: Int): JOptionC[Val] =
    addressCallFrame.getLocal(x).flatMap(getByVirt).asInstanceOf

  override def getLocalByName(x: Var): JOptionC[Val] =
    addressCallFrame.getLocalByName(x).flatMap(getByVirt).asInstanceOf

  private def getByVirt(virts: PowVirtAddr): JOptionC[Val] =
    apronState.recencyStore.read(virts).asInstanceOf[JOptionA[Val]].toJOptionC

  override def withNew[A](d: Data, vars: Iterable[(Var, Option[Val])], site: CallSite)(f: => A): A =
    val virtAddrs = vars.map((variable, _) =>
      val ctx = localVariableAllocator.alloc((variable, d))
      val virt = apronState.recencyStore.alloc(ctx)
      (variable, Some(PowVirtualAddress(virt)))
    )
    addressCallFrame.withNew(d, virtAddrs, site) {
      for ((variable, exprOption) <- vars; expr <- exprOption)
        setLocalByName(variable, expr)
      f
    }

  private def findName(idx: Int): Var =
    addressCallFrame.getFrameNames.find((name,idx2) => idx == idx2)
      .getOrElse(throw new IllegalStateException(s"No name bound to index $idx not bound in call frame $this"))
      ._1

  override type State = RelationalCallFrameState

  case class RelationalCallFrameState(addressCallFrameState: addressCallFrame.State):
    override def equals(obj: Any): Boolean =
      obj match {
        case other: RelationalCallFrameState => MapEquals(this.addressCallFrameState, other.addressCallFrameState)
        case _ => false
      }

  override def getState: State =
    RelationalCallFrameState(addressCallFrame.getState)

  override def setState(state: State): Unit =
    addressCallFrame.setState(state.addressCallFrameState)

  override def setBottom: Unit =
    addressCallFrame.setBottom

  override def join: Join[State] =
//    implicitly[Join[State]]
    combineCallFrame

  override def widen: Widen[State] =
//    implicitly[Widen[State]]
    combineCallFrame

  override def stackWiden: StackWidening[State] =
    (stack: List[State], call: State) =>
      Unchanged(call)
//      if(stack.contains(call))
//        Unchanged(call)
//      else
//        Changed(call)

  def combineCallFrame[W <: Widening]: Combine[State, W] = (s1: State, s2: State) =>
    var changed = false
    val joined = s1.addressCallFrameState.unionWith(s2.addressCallFrameState, (idx, virts1, virts2) =>
      val variable = addressCallFrame.getFrameNames.find(_._2 == idx).get._1
      val phys1 = apronState.relationalStore.withLeftState(st => (virts1.physicalAddressesPure(st.addressTranslationState),st))
      val phys2 = apronState.relationalStore.withRightState(st => (virts2.physicalAddressesPure(st.addressTranslationState),st))
      if(ssa && phys1 != phys2) {
        val ctx = localVariableAllocator((variable,addressCallFrame.data))
        val (result1,result1Phys) = apronState.relationalStore.withLeftState(state1 =>
          val (result,state2) = apronState.recencyStore.allocPure(ctx, state1.asInstanceOf[apronState.recencyStore.State])
          val physFrom = virts1.physicalAddressesPure(state2.asInstanceOf[apronState.relationalStore.State].addressTranslationState)
          val state3 = apronState.relationalStore.expandPure(PowersetAddr(physFrom), PhysicalAddress(result.ctx, Recency.Recent), state2.asInstanceOf[apronState.relationalStore.State])
          ((PowVirtualAddress(result),PhysicalAddress(result.ctx, Recency.Recent)),state3)
        )
        val result2 = apronState.relationalStore.withRightState(state1 =>
          val (result,state2) = apronState.recencyStore.allocPure(ctx, state1.asInstanceOf[apronState.recencyStore.State])
          val physFrom = virts2.physicalAddressesPure(state2.asInstanceOf[apronState.relationalStore.State].addressTranslationState)
          val state3 = apronState.relationalStore.expandPure(PowersetAddr(physFrom), PhysicalAddress(result.ctx, Recency.Recent), state2.asInstanceOf[apronState.relationalStore.State])
          (PowVirtualAddress(result),state3)
        )
        changed ||= !phys1.contains(result1Phys)
        result1.union(result2)
      } else {
        val joined = Join(virts1,virts2)
        changed ||= joined.hasChanged
        joined.get
      }
    )
    
    MaybeChanged(RelationalCallFrameState(joined), changed)

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    addressCallFrame.getState.iterator.flatMap(valueIterator)

  def isSound[cData, cVal](c: ConcreteCallFrame[cData, Var, cVal, CallSite])(using vSoundness: Soundness[cVal, Val], dSoundness: Soundness[cData, Data]): IsSound =
    val dataIsSound = dSoundness.isSound(c.data, data)
    if (dataIsSound.isNotSound)
      return dataIsSound
    if (addressCallFrame.getFrameNames != c.getFrameNames)
      return IsSound.NotSound(s"Variable names in call frame differ: concrete=${c.getFrameNames}, abstract=$addressCallFrame.getFrameNames")
    val cVals: Array[cVal] = c.getVars
    val aVals: ArraySeq[Val] = getVars
    seqIsSound.isSound(cVals, aVals)

  override def toString: String = s"$addressCallFrame, ${apronState.recencyStore.addressTranslation}, ${apronState.relationalStore}"

object RelationalCallFrame:

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
     combineExpressionAllocator: Allocator[Ctx, (ApronExpr[VirtualAddress[Ctx], Type], ApronExpr[VirtualAddress[Ctx],Type])],
     localVariableAllocator: Allocator[Ctx, (Var, Data)],
     apronManager: Manager,
     effectStack: EffectStack,
     withWideningThresholds: WithWideningThresholds
   ): (RelationalCallFrame[Data, Var, ApronExpr[VirtualAddress[Ctx], Type], CallSite, Ctx, Type], ApronRecencyState[Ctx, Type, ApronExpr[VirtualAddress[Ctx], Type]]) =
    val state = RecencyRelationalStore[Ctx,Type]
    given Lazy[ApronState[VirtualAddress[Ctx],Type]] = lazily(state)
    val callFrame = new RelationalCallFrame[Data, Var, ApronExpr[VirtualAddress[Ctx], Type], CallSite, Ctx, Type](initData, initVars, localVariableAllocator, state)
    (callFrame, state)