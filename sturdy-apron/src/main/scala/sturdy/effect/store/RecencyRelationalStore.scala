package sturdy.effect.store

import apron.*
import sturdy.apron.{ApronExpr, ApronExprConverter, ApronRecencyState, ApronState, ApronType, ApronVar, StatefullRelationalExprT, StatelessRelationalExpr, given}
import sturdy.effect.{EffectStack, Stateless}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyStore, RelationalStore, given}
import sturdy.util.{Lazy, lazily}
import sturdy.values.floating.FloatSpecials
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
    (using
     apronManager: Manager,
     virtRelationalValue: StatelessRelationalExpr[Val, VirtualAddress[Ctx], Type]
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
    given lazyConvertExpr: Lazy[ApronExprConverter[Ctx, Type, Val]] = Lazy(convertExpr)
    given relationalValue: StatefullRelationalExprT[Val, PhysicalAddress[Ctx], Type, RelationalStoreState[Ctx, Map[PhysicalAddress[Ctx], (FloatSpecials, Type)]]] = RelationalValueApronExprPhysicalAddress[Val, Ctx, Type].asInstanceOf
    val relationalStore: RelationalStore[Ctx, Type, PowPhysAddr, Val] = new RelationalStore[Ctx, Type, PowPhysAddr, Val](
      Map(),
      apronManager,
      Abstract1(apronManager, new Environment()),
      Map()
    )

    import relationalStore.given
    val recencyStore: RecencyStore[Ctx, PowVirtAddr, Val] = RecencyStore(relationalStore)

    convertExpr = new ApronExprConverter[Ctx, Type, Val](recencyStore, relationalStore)

    (recencyStore, relationalStore)

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
      val (recencyStore, relationalStore) = apply[Ctx, Type, ApronExpr[VirtualAddress[Ctx],Type]]
      apronState = new ApronRecencyState(temporaryVariableAllocator, recencyStore, relationalStore)

      apronState
