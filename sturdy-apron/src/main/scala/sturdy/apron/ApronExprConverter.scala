package sturdy.apron

import sturdy.effect.store.{RecencyStore, RelationalStore}
import sturdy.values.{Join, Widen}
import sturdy.values.references.{PhysicalAddress, PowRecency, PowVirtualAddress, PowersetAddr, Recency, RecencyRegion, VirtualAddress, given}

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
  def virtToPhys(virtAddr: VirtualAddress[Ctx]): PhysicalAddress[Ctx] =
    virtAddr.recency match
      case PowRecency.Recent =>
        PhysicalAddress(virtAddr.ctx, Recency.Recent)
      case PowRecency.Old =>
        PhysicalAddress(virtAddr.ctx, Recency.Old)
      case PowRecency.RecentOld =>
        recencyStore.joinRecentIntoOld(PowVirtualAddress(virtAddr))
        PhysicalAddress(virtAddr.ctx, Recency.Old)

  def virtToPhys(exprVirtAddr: ApronExpr[VirtualAddress[Ctx], Type]): ApronExpr[PhysicalAddress[Ctx], Type] =
    exprVirtAddr.mapAddr(virtToPhys)

  def virtToPhys(constrVirtAddr: ApronCons[VirtualAddress[Ctx], Type]): ApronCons[PhysicalAddress[Ctx], Type] =
    constrVirtAddr.mapAddr(virtToPhys)

  def physToVirt(phys: PhysicalAddress[Ctx]): VirtualAddress[Ctx] =
    val region = recencyStore.addressTranslation.region(phys.ctx).getOrElse(
      RecencyRegion()
    )
    phys.recency match
      case Recency.Recent =>
        if (region.recent.isEmpty)
          recencyStore.addressTranslation.allocNoRetire(phys.ctx)
        else
          VirtualAddress(phys.ctx, region.recent.max, recencyStore.addressTranslation)
      case Recency.Old =>
        if (region.old.isEmpty)
          val recentVirts = recencyStore.addressTranslation.virtualAddresses(phys.ctx)
          recencyStore.joinRecentIntoOld(recentVirts)
          recentVirts.virtualAddresses.head
        else
          VirtualAddress(phys.ctx, region.old.max, recencyStore.addressTranslation)

  def physToVirt(physExpr: ApronExpr[PhysicalAddress[Ctx], Type]): ApronExpr[VirtualAddress[Ctx], Type] =
    physExpr.mapAddr(physToVirt)

  def physToVirt(physCons: ApronCons[PhysicalAddress[Ctx], Type]): ApronCons[VirtualAddress[Ctx], Type] =
    physCons.mapAddr(physToVirt)