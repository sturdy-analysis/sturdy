package sturdy.effect.callframe

import apron.*
import sturdy.{IsSound, Soundness, seqIsSound}
import sturdy.apron.{ApronCons, ApronExpr, ApronRecencyState, ApronState, ApronType, ApronVar, IntApronType, RelationalValue, given}
import sturdy.data.{JOption, JOptionA, JOptionC, NoJoin, WithJoin, given}
import sturdy.effect.EffectStack
import sturdy.effect.allocation.Allocator
import sturdy.effect.callframe.{ConcreteCallFrame, JoinableDecidableCallFrame, MutableCallFrame}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
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
    val localVariableAllocator: Allocator[Ctx, (Var,Data,Option[CallSite])],
    val apronState: ApronRecencyState[Ctx, Type, Val]
  )(using
    relationalValue: RelationalValue[Val, VirtualAddress[Ctx], Type]
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

  def getAddressTranslation: AddressTranslation[Ctx] = apronState.recencyStore.getAddressTranslation

  def setVars(newVars: Iterable[(Var, Option[Val])]) =
    addressCallFrame.setVars(
      newVars.map((variable, _) =>
        val ctx = localVariableAllocator.alloc((variable, addressCallFrame.data, addressCallFrame.callSite))
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
      val ctx = localVariableAllocator.alloc((name, addressCallFrame.data, addressCallFrame.callSite))
      val freshVirt = PowVirtualAddress(apronState.recencyStore.alloc(ctx))
      addressCallFrame.setLocal(idx, freshVirt)
      apronState.recencyStore.write(freshVirt, v)
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
    virts.reduce {
      virt =>
        val v1 = apronState.relationalStore.getMetaData(virt.physical).map((floatSpecials,tpe) => relationalValue.makeRelationalVal(ApronExpr.Addr(virt, floatSpecials, tpe)))
        val v2 = apronState.relationalStore.nonRelationalStore.read(virt.physical)
        Join(v1, v2).get
    }.toJOptionC

  override def withNew[A](d: Data, vars: Iterable[(Var, Option[Val])], site: CallSite)(f: => A): A =
    val virtAddrs = vars.map((variable, _) =>
      val ctx = localVariableAllocator.alloc((variable, d, Some(site)))
      (variable, Some(PowVirtualAddress(apronState.recencyStore.alloc(ctx))))
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

  override type State = addressCallFrame.State

  override def getState: State =
    addressCallFrame.getState

  override def setState(state: State): Unit =
    addressCallFrame.setState(state)

  override def join: Join[State] = implicitly[Join[State]]
  override def widen: Widen[State] = implicitly[Widen[State]]
  override def stackWiden: StackWidening[State] =
    (stack: List[State], call: State) =>
//      Unchanged(call)
      if(stack.contains(call))
        Unchanged(call)
      else
        Changed(call)

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
     localVariableAllocator: Allocator[Ctx, (Var, Data, Option[CallSite])],
     apronManager: Manager,
     effectStack: EffectStack
   ): (RelationalCallFrame[Data, Var, ApronExpr[VirtualAddress[Ctx], Type], CallSite, Ctx, Type], ApronRecencyState[Ctx, Type, ApronExpr[VirtualAddress[Ctx], Type]]) =
    val state = RecencyRelationalStore[Ctx,Type]
    given Lazy[ApronState[VirtualAddress[Ctx],Type]] = lazily(state)
    val callFrame = new RelationalCallFrame[Data, Var, ApronExpr[VirtualAddress[Ctx], Type], CallSite, Ctx, Type](initData, initVars, localVariableAllocator, state)
    (callFrame, state)