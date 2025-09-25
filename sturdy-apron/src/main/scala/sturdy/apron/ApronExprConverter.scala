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

  import relationalStore.given
  val addrTrans = recencyStore.addressTranslation
  final type State = relationalStore.State

  def virtToPhysPure(virtAddr: VirtualAddress[Ctx], state: State): (PhysicalAddress[Ctx],State) =
    addrTrans.recency[State](virtAddr.ctx, virtAddr.n, state) match
      case PowRecency.Recent =>
        (PhysicalAddress(virtAddr.ctx, Recency.Recent), state)
      case PowRecency.Old =>
        (PhysicalAddress(virtAddr.ctx, Recency.Old), state)
      case PowRecency.RecentOld =>
        (PhysicalAddress(virtAddr.ctx, Recency.Old), recencyStore.joinRecentIntoOldPure(PowVirtualAddress(virtAddr), state.asInstanceOf[recencyStore.State]).asInstanceOf[State])
      case PowRecency.Failed =>
        (PhysicalAddress(virtAddr.ctx, Recency.Failed), state)

  inline def virtToPhysPure(exprVirtAddr: ApronExpr[VirtualAddress[Ctx], Type], state0: State): (ApronExpr[PhysicalAddress[Ctx], Type],State) =
    if(exprVirtAddr.isConstant) {
      (exprVirtAddr.asInstanceOf[ApronExpr[PhysicalAddress[Ctx], Type]],state0)
    } else {
      var state = state0
      val expr = exprVirtAddr.mapAddr { addr =>
        val (addr1, state1) = virtToPhysPure(addr, state)
        state = state1
        addr1
      }
      (expr, state)
    }

  inline def virtToPhysPure(constrVirtAddr: ApronCons[VirtualAddress[Ctx], Type], state0: State): (ApronCons[PhysicalAddress[Ctx], Type],State) =
    if(constrVirtAddr.isConstant) {
      (constrVirtAddr.asInstanceOf[ApronCons[PhysicalAddress[Ctx], Type]],state0)
    } else {
      var state = state0
      val cons = constrVirtAddr.mapAddr { addr0 =>
        val (addr1, state1) = virtToPhysPure(addr0, state)
        state = state1
        addr1
      }
      (cons, state)
    }

  def virtToPhys(virtAddr: VirtualAddress[Ctx]): PhysicalAddress[Ctx] =
    relationalStore.withInternalState(virtToPhysPure(virtAddr, _))

  def virtToPhys(exprVirtAddr: ApronExpr[VirtualAddress[Ctx], Type]): ApronExpr[PhysicalAddress[Ctx], Type] =
    relationalStore.withInternalState(virtToPhysPure(exprVirtAddr, _))

  def virtToPhys(constrVirtAddr: ApronCons[VirtualAddress[Ctx], Type]): ApronCons[PhysicalAddress[Ctx], Type] =
    relationalStore.withInternalState(virtToPhysPure(constrVirtAddr, _))

  def physToVirtPure(phys: PhysicalAddress[Ctx], state: State): (VirtualAddress[Ctx],State) =
    val region = state.addressTranslationState.mapping.getOrElse(phys.ctx,
      throw new IllegalStateException(s"$phys not found in ${recencyStore.addressTranslation.getState}")
    )
    phys.recency match
      case Recency.Recent =>
        if (region.recent.isEmpty)
          recencyStore.addressTranslation.allocNoRetire(phys.ctx, PowRecency.Recent, state)
        else
          (VirtualAddress(phys.ctx, region.recent.max, recencyStore.addressTranslation), state)
      case Recency.Old =>
        if (region.old.isEmpty)
          val recentVirts = recencyStore.addressTranslation.virtualAddresses(phys.ctx)
          (recentVirts.virtualAddresses.head, recencyStore.joinRecentIntoOldPure(recentVirts, state.asInstanceOf).asInstanceOf)
        else
          (VirtualAddress(phys.ctx, region.old.max, recencyStore.addressTranslation), state)
      case Recency.Failed =>
        if (region.failed.isEmpty)
          recencyStore.addressTranslation.allocNoRetire(phys.ctx, PowRecency.Failed, state)
        else
          (VirtualAddress(phys.ctx, region.failed.max, recencyStore.addressTranslation), state)

  inline def physToVirtPure(physExpr: ApronExpr[PhysicalAddress[Ctx], Type], state0: State): (ApronExpr[VirtualAddress[Ctx], Type],State) =
    if(physExpr.isConstant) {
      (physExpr.asInstanceOf[ApronExpr[VirtualAddress[Ctx], Type]],state0)
    } else {
      var state = state0
      val virtExpr = physExpr.mapAddr { addr =>
        val (addr1, state1) = physToVirtPure(addr, state)
        state = state1
        addr1
      }
      (virtExpr, state)
    }

  inline def physToVirtPure(physCons: ApronCons[PhysicalAddress[Ctx], Type], state0: State): (ApronCons[VirtualAddress[Ctx], Type],State) =
    var state = state0
    val virtCons = physCons.mapAddr { addr =>
        val (addr1,state1) = physToVirtPure(addr, state)
        state = state1
        addr1
    }
    (virtCons,state)

  inline def physToVirt(phys: PhysicalAddress[Ctx]): VirtualAddress[Ctx] =
    relationalStore.withInternalState(physToVirtPure(phys, _))

  inline def physToVirt(physExpr: ApronExpr[PhysicalAddress[Ctx], Type]): ApronExpr[VirtualAddress[Ctx], Type] =
    relationalStore.withInternalState(physToVirtPure(physExpr, _))

  inline def physToVirt(physCons: ApronCons[PhysicalAddress[Ctx], Type]): ApronCons[VirtualAddress[Ctx], Type] =
    relationalStore.withInternalState(physToVirtPure(physCons, _))