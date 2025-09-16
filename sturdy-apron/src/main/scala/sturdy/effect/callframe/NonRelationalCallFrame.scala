package sturdy.effect.callframe

import sturdy.apron.ApronType
import sturdy.data.{JOption, JOptionA, JOptionC, NoJoin}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.RelationalStore
import sturdy.values.references.Recency.Recent
import sturdy.values.references.{PhysicalAddress, PowersetAddr}
import sturdy.values.{*, given}

final class NonRelationalCallFrame[
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
    relationalStore: RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx],PhysicalAddress[Ctx]], Val]
  )
  extends MutableCallFrame[Data, Var, Val, CallSite, NoJoin] with DecidableCallFrame[Data, Var, Val, CallSite]:

  val ctxFrame: JoinableDecidableCallFrame[Data, Var, Powerset[Ctx], CallSite] =
    JoinableDecidableCallFrame(
      initData,
      Iterable.empty
    )

  override def data: Data = ctxFrame.data
  override def setLocal(idx: Int, v: Val): JOption[NoJoin, Unit] =
    ctxFrame.getLocal(idx).map(ctxs =>
      relationalStore.write(new PowersetAddr(ctxs.set.map(ctx => PhysicalAddress(ctx, Recent))), v)
    )

  override def setLocalByName(x: Var, v: Val): JOption[NoJoin, Unit] =
    ctxFrame.getFrameNames.get(x) match
      case None => JOptionC.none
      case Some(idx) => setLocal(idx, v)

  override def getLocal(x: Int): JOption[NoJoin, Val] =
    ctxFrame.getLocal(x).flatMap(getByCtxs)

  override def getLocalByName(x: Var): JOption[NoJoin, Val] =
    ctxFrame.getLocalByName(x).flatMap(getByCtxs)

  private def getByCtxs(ctxs: Powerset[Ctx]): JOptionC[Val] =
    relationalStore.read(new PowersetAddr(ctxs.set.map(ctx => PhysicalAddress(ctx, Recent)))).asInstanceOf[JOptionA[Val]].toJOptionC

  override def withNew[A](d: Data, vars: Iterable[(Var, Option[Val])], site: CallSite)(f: => A): A =
    val ctxs = vars.map((variable, _) =>
      val ctx = localVariableAllocator.alloc((variable, d, Some(site)))
      (variable, Some(Powerset(ctx)))
    )
    ctxFrame.withNew(d, ctxs, site) {
      for((x,valOpt) <- vars; value <- valOpt)
        setLocalByName(x, value)
      f
    }

  override type State = ctxFrame.State

  override def getState: State = ctxFrame.getState

  override def setState(st: State): Unit = ctxFrame.setState(st)

  override def join: Join[State] = ctxFrame.join

  override def widen: Widen[State] = ctxFrame.widen