package sturdy.effect.store

import apron.*
import sturdy.apron.{ApronExpr, ApronExprConverter, ApronRecencyState, ApronState, ApronType, ApronVar, given}
import sturdy.effect.{EffectStack, Stateless}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyStore, RelationalStore, given}
import sturdy.util.{Lazy, lazily}
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
      _getRelationalVal: (ApronExprConverter[Ctx, Type, Val],Val) => Option[ApronExpr[PhysicalAddress[Ctx], Type]],
      _makeRelationalVal: (ApronExprConverter[Ctx, Type, Val], ApronExpr[PhysicalAddress[Ctx], Type]) => Val
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

    var convertExpr: ApronExprConverter[Ctx, Type, Val] = null

    val apronStore: RelationalStore[Ctx, Type, PowPhysAddr, Val] = new RelationalStore[Ctx, Type, PowPhysAddr, Val](
      apronManager,
      Abstract1(apronManager, new Environment()),
      Map()
    ):
      override def getRelationalVal(v:Val): Option[ApronExpr[PhysicalAddress[Ctx], Type]] =
        _getRelationalVal(convertExpr, v)

      override def makeRelationalVal(expr: ApronExpr[PhysicalAddress[Ctx], Type]): Val =
        _makeRelationalVal(convertExpr, expr)

    val addressTranslation: AddressTranslation[Ctx] = AddressTranslation.empty

    val recencyStore: RecencyStore[Ctx, PowVirtAddr, Val] =
      RecencyStore(
        apronStore,
        addressTranslation)

    convertExpr = new ApronExprConverter[Ctx, Type, Val](recencyStore, apronStore)

    (recencyStore, apronStore)

  def apply
    [
      Ctx: Ordering : Finite,
      Type: ApronType : Join : Widen
    ]
    (using
      temporaryVariableAllocator: Allocator[Ctx, Type],
      apronManager: Manager,
      effectStack: EffectStack
    ):
    ApronRecencyState[Ctx, Type, ApronExpr[VirtualAddress[Ctx], Type]] =
      var apronState: ApronRecencyState[Ctx, Type, ApronExpr[VirtualAddress[Ctx], Type]] = null
      given Lazy[ApronState[VirtualAddress[Ctx], Type]] = lazily(apronState)
      val (recencyStore, relationalStore) = apply[Ctx, Type, ApronExpr[VirtualAddress[Ctx],Type]](
        _getRelationalVal = (convertExpr, expr) => Some(convertExpr.virtToPhys(expr)),
        _makeRelationalVal = (convertExpr, expr) => convertExpr.physToVirt(expr)
      )
      apronState = new ApronRecencyState(temporaryVariableAllocator, recencyStore, relationalStore)

      apronState
