package sturdy.effect.store

import apron.*
import sturdy.apron.{ApronExpr, ApronRecencyState, ApronState, ApronType, given}
import sturdy.effect.Stateless
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyStore, RelationalStore, given}
import sturdy.values.{Combine, Finite, Join, MaybeChanged, Widen, Widening}
import sturdy.values.references.{*, given}

import scala.reflect.ClassTag

object RecencyRelationalStore:
  def apply
    [
      Ctx: Ordering: Finite,
      Type: ApronType: Join: Widen,
      Val: Join: Widen
    ]
    (
      _getRelationalVal: Val => Option[ApronExpr[PhysicalAddress[Ctx], Type]],
      _makeRelationalVal: (RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], Val], ApronExpr[PhysicalAddress[Ctx], Type]) => Val
    )
    (using
     apronManager: Manager,
    ):
    (
      RecencyStore[Ctx, PowVirtualAddress[Ctx], Val],
      RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], Val]
    ) =
    type VirtAddr = VirtualAddress[Ctx]
    type PhysAddr = PhysicalAddress[Ctx]
    type PowPhysAddr = PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]]
    type PowVirtAddr = PowVirtualAddress[Ctx]
    type ApronExprVirtAddr = ApronExpr[VirtualAddress[Ctx], Type]
    type ApronExprPhysAddr = ApronExpr[PhysicalAddress[Ctx], Type]

    val apronStore: RelationalStore[Ctx, Type, PowPhysAddr, Val] = new RelationalStore[Ctx, Type, PowPhysAddr, Val](
      apronManager,
      Abstract1(apronManager, new Environment()),
      Map()
    ):
      override def getRelationalVal(v:Val): Option[ApronExpr[PhysicalAddress[Ctx], Type]] =
        _getRelationalVal(v)

      override def makeRelationalVal(expr: ApronExpr[PhysicalAddress[Ctx], Type]): Val =
        _makeRelationalVal(this, expr)

    val addressTranslation: AddressTranslation[Ctx] = AddressTranslation.empty

    val recencyStore: RecencyStore[Ctx, PowVirtAddr, Val] =
      RecencyStore(
        apronStore,
        addressTranslation)

    (recencyStore, apronStore)

  def apply
    [
      Ctx: Ordering : Finite,
      Type: ApronType : Join : Widen
    ]
    (using
      apronManager: Manager
    ):
    (
      RecencyStore[Ctx, PowVirtualAddress[Ctx], ApronExpr[PhysicalAddress[Ctx], Type]],
      RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], ApronExpr[PhysicalAddress[Ctx], Type]]
    ) =
    ???