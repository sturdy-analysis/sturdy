package sturdy.apron

import sturdy.util.Lazy
import sturdy.values.references.{AddressTranslationState, PhysicalAddress, VirtualAddress}

type StatefullRelationalExprT[Val,Addr,Type, _State] = StatefullRelationalExpr[Val,Addr,Type] { type State = _State }
trait StatefullRelationalExpr[Val, Addr, Type]:
  type State
  def getRelationalExprPure(v: Val, state: State): (Option[ApronExpr[Addr, Type]],State)
  def makeRelationalExprPure(expr: ApronExpr[Addr,Type], state: State): (Val,State)
  inline def getRelationalExpr(v: Val): Option[ApronExpr[Addr, Type]] = withInternalState(getRelationalExprPure(v, _))
  inline def makeRelationalExpr(expr: ApronExpr[Addr, Type]): Val = withInternalState(makeRelationalExprPure(expr, _))
  def withInternalState[A](f: State => (A,State)): A

trait StatelessRelationalExpr[Val, Addr, Type]:
  def getRelationalExpr(v: Val): Option[ApronExpr[Addr,Type]]
  def makeRelationalExpr(expr: ApronExpr[Addr,Type]): Val

given RelationalValueApronExpr[Addr, Type]: StatelessRelationalExpr[ApronExpr[Addr,Type], Addr, Type] with
  override inline def getRelationalExpr(v: ApronExpr[Addr, Type]): Option[ApronExpr[Addr, Type]] = Some(v)
  override inline def makeRelationalExpr(expr: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = expr

given RelationalValueApronExprPhysicalAddress[Val, Ctx, Type]
  (using
   virtRelationalValue: StatelessRelationalExpr[Val, VirtualAddress[Ctx], Type],
   convertExpr: Lazy[ApronExprConverter[Ctx, Type, Val]]
  ): StatefullRelationalExpr[Val, PhysicalAddress[Ctx], Type] with
  type State = convertExpr.value.State

  override def getRelationalExprPure(v: Val, state: State): (Option[ApronExpr[PhysicalAddress[Ctx], Type]],State) = {
    virtRelationalValue.getRelationalExpr(v) match
      case Some(expr) =>
        val (ret, convertExprState) = convertExpr.value.virtToPhysPure(expr, state)
        (Some(ret), convertExprState)
      case None => (None, state)
  }

  override def makeRelationalExprPure(expr: ApronExpr[PhysicalAddress[Ctx], Type], state: State): (Val, State) =
    (virtRelationalValue.makeRelationalExpr(convertExpr.value.physToVirt(expr)), state)

  override def withInternalState[A](f: (State) => (A, State)): A =
    convertExpr.value.addrTrans.withInternalState(addrTransState1 =>
      convertExpr.value.recencyStore.withInternalState(recStoreState1 =>
        val (a,(addrTransState2,recStoreState2)) = f((addrTransState1,recStoreState1))
        ((a, addrTransState2),recStoreState2)
      )
    )