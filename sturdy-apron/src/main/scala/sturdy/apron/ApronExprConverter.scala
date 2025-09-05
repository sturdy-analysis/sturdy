package sturdy.apron

import sturdy.effect.store.{RecencyStore, RelationalStore}
import sturdy.values.references.Recency.Recent
import sturdy.values.{Join, Widen}
import sturdy.values.references.{AddressTranslationState, PhysicalAddress, PowRecency, PowVirtualAddress, PowersetAddr, Recency, RecencyRegion, VirtualAddress, given}

/**
 * Converts ApronExprs over VirtualAddresses to ApronExprs over PhysicalAddresses and vice versa
 */
case class ApronExprConverter
  [
    Ctx: Ordering,
    Type: ApronType : Join,
    Val: Join: Widen
  ]
  (
    recencyStore: RecencyStore[Ctx, PowVirtualAddress[Ctx], Val],
    relationalStore: RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], Val]
  ):

  val addrTrans = recencyStore.addressTranslation

  final type State = relationalStore.State

  def virtToPhysPure(virtAddr: VirtualAddress[Ctx], state: State): (PhysicalAddress[Ctx],State) =
    virtAddr.addressTrans.recency(virtAddr.ctx, virtAddr.n, state._1) match
      case PowRecency.Recent =>
        (PhysicalAddress(virtAddr.ctx, Recency.Recent), state)
      case PowRecency.Old =>
        (PhysicalAddress(virtAddr.ctx, Recency.Old), state)
      case PowRecency.RecentOld =>
        (PhysicalAddress(virtAddr.ctx, Recency.Old), recencyStore.joinRecentIntoOldPure(state, PowVirtualAddress(virtAddr)))
      case PowRecency.Failed =>
        (PhysicalAddress(virtAddr.ctx, Recency.Failed), state)

  inline def virtToPhysPure(exprVirtAddr: ApronExpr[VirtualAddress[Ctx], Type], state0: State): (ApronExpr[PhysicalAddress[Ctx], Type],State) = {
    var state = state0
    val expr = exprVirtAddr.mapAddr { addr =>
      val (addr1, state1) = virtToPhysPure(addr, state)
      state = state1
      addr1
    }
    (expr,state)
  }

  inline def virtToPhysPure(constrVirtAddr: ApronCons[VirtualAddress[Ctx], Type], state0: State): (Option[ApronCons[PhysicalAddress[Ctx], Type]],State) =
    var state = state0
    if(constrVirtAddr.addrs.forall(virt =>
      virt.addressTrans.recency(virt.ctx, virt.n, state._1) == PowRecency.Recent
    )) {
      val cons = constrVirtAddr.mapAddr { addr0 =>
        val (addr1, state1) = virtToPhysPure(addr0,state)
        state = state1
        addr1
      }
      (Some(cons), state)
    } else
      (None, state)

  def virtToPhys(virtAddr: VirtualAddress[Ctx]): PhysicalAddress[Ctx] =
    relationalStore.withInternalState(virtToPhysPure(virtAddr, _))

  def virtToPhys(exprVirtAddr: ApronExpr[VirtualAddress[Ctx], Type]): ApronExpr[PhysicalAddress[Ctx], Type] =
    relationalStore.withInternalState(virtToPhysPure(exprVirtAddr, _))

  def virtToPhys(constrVirtAddr: ApronCons[VirtualAddress[Ctx], Type]): Option[ApronCons[PhysicalAddress[Ctx], Type]] =
    relationalStore.withInternalState(virtToPhysPure(constrVirtAddr, _))

  def physToVirt(state: State, phys: PhysicalAddress[Ctx]): (VirtualAddress[Ctx],State) =
    val region = state.addressTranslationState.mapping.getOrElse(phys.ctx,
      throw new IllegalStateException(s"$phys not found in ${recencyStore.addressTranslation.getState}")
    )
    phys.recency match
      case Recency.Recent =>
        if (region.recent.isEmpty)
          recencyStore.addressTranslation.allocNoRetire(phys.ctx, PowRecency.Recent)
        else
          VirtualAddress(phys.ctx, region.recent.max, recencyStore.addressTranslation)
      case Recency.Old =>
        if (region.old.isEmpty)
          val recentVirts = recencyStore.addressTranslation.virtualAddresses(phys.ctx)
          recencyStore.joinRecentIntoOld(recentVirts)
          recentVirts.virtualAddresses.head
        else
          VirtualAddress(phys.ctx, region.old.max, recencyStore.addressTranslation)
      case Recency.Failed =>
        if (region.failed.isEmpty)
          recencyStore.addressTranslation.allocNoRetire(phys.ctx, PowRecency.Failed)
        else
          VirtualAddress(phys.ctx, region.failed.max, recencyStore.addressTranslation)

  inline def physToVirt(state: AddressTranslationState[Ctx], physExpr: ApronExpr[PhysicalAddress[Ctx], Type]): ApronExpr[VirtualAddress[Ctx], Type] =
    physExpr.mapAddr(physToVirt(state,_))

  inline def physToVirt(state: AddressTranslationState[Ctx], physCons: ApronCons[PhysicalAddress[Ctx], Type]): ApronCons[VirtualAddress[Ctx], Type] =
    physCons.mapAddr(physToVirt(state,_))

  inline def physToVirt(phys: PhysicalAddress[Ctx]): VirtualAddress[Ctx] =
    physToVirt(addrTrans.internalState, phys)

  inline def physToVirt(physExpr: ApronExpr[PhysicalAddress[Ctx], Type]): ApronExpr[VirtualAddress[Ctx], Type] =
    physToVirt(addrTrans.internalState, physExpr)

  inline def physToVirt(physCons: ApronCons[PhysicalAddress[Ctx], Type]): ApronCons[VirtualAddress[Ctx], Type] =
    physToVirt(addrTrans.internalState, physCons)