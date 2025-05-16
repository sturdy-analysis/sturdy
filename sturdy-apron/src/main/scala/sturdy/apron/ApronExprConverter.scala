package sturdy.apron

import sturdy.effect.store.{RecencyStore, RelationalStore}
import sturdy.values.references.Recency.Recent
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

  val addrTrans = recencyStore.addressTranslation

  def virtToPhys(mapping: Map[Ctx, RecencyRegion], virtAddr: VirtualAddress[Ctx]): PhysicalAddress[Ctx] =
    virtAddr.addressTrans.recency(mapping, virtAddr.ctx, virtAddr.n) match
      case PowRecency.Recent =>
        PhysicalAddress(virtAddr.ctx, Recency.Recent)
      case PowRecency.Old =>
        PhysicalAddress(virtAddr.ctx, Recency.Old)
      case PowRecency.RecentOld =>
        recencyStore.joinRecentIntoOld(mapping, PowVirtualAddress(virtAddr))
        PhysicalAddress(virtAddr.ctx, Recency.Old)
      case PowRecency.Failed =>
        PhysicalAddress(virtAddr.ctx, Recency.Failed)

  inline def virtToPhys(mapping: Map[Ctx, RecencyRegion], exprVirtAddr: ApronExpr[VirtualAddress[Ctx], Type]): ApronExpr[PhysicalAddress[Ctx], Type] =
    exprVirtAddr.mapAddr(virtToPhys(mapping,_))

  inline def virtToPhys(mapping: Map[Ctx, RecencyRegion], constrVirtAddr: ApronCons[VirtualAddress[Ctx], Type]): Option[ApronCons[PhysicalAddress[Ctx], Type]] =
    if(constrVirtAddr.addrs.forall(virt =>
      virt.addressTrans.recency(mapping, virt.ctx, virt.n) == PowRecency.Recent
    ))
      Some(constrVirtAddr.mapAddr(virtToPhys(mapping,_)))
    else
      None

  inline def virtToPhys(virtAddr: VirtualAddress[Ctx]): PhysicalAddress[Ctx] =
    virtToPhys(addrTrans.mapping, virtAddr)

  inline def virtToPhys(exprVirtAddr: ApronExpr[VirtualAddress[Ctx], Type]): ApronExpr[PhysicalAddress[Ctx], Type] =
    virtToPhys(addrTrans.mapping, exprVirtAddr)

  inline def virtToPhys(constrVirtAddr: ApronCons[VirtualAddress[Ctx], Type]): Option[ApronCons[PhysicalAddress[Ctx], Type]] =
    virtToPhys(addrTrans.mapping, constrVirtAddr)

  def physToVirt(mapping: Map[Ctx, RecencyRegion], phys: PhysicalAddress[Ctx]): VirtualAddress[Ctx] =
    val region = mapping.getOrElse(phys.ctx,
      throw new IllegalStateException(s"$phys not found in ${recencyStore.addressTranslation.getState}")
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

  inline def physToVirt(mapping: Map[Ctx, RecencyRegion], physExpr: ApronExpr[PhysicalAddress[Ctx], Type]): ApronExpr[VirtualAddress[Ctx], Type] =
    physExpr.mapAddr(physToVirt(mapping,_))

  inline def physToVirt(mapping: Map[Ctx, RecencyRegion], physCons: ApronCons[PhysicalAddress[Ctx], Type]): ApronCons[VirtualAddress[Ctx], Type] =
    physCons.mapAddr(physToVirt(mapping,_))

  inline def physToVirt(phys: PhysicalAddress[Ctx]): VirtualAddress[Ctx] =
    physToVirt(addrTrans.mapping, phys)

  inline def physToVirt(physExpr: ApronExpr[PhysicalAddress[Ctx], Type]): ApronExpr[VirtualAddress[Ctx], Type] =
    physToVirt(addrTrans.mapping, physExpr)

  inline def physToVirt(physCons: ApronCons[PhysicalAddress[Ctx], Type]): ApronCons[VirtualAddress[Ctx], Type] =
    physToVirt(addrTrans.mapping, physCons)