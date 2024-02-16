package sturdy.effect.store

import apron.*
import sturdy.apron.{ApronExpr, ApronRecencyState, ApronState, ApronType, given}
import sturdy.effect.Stateless
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{ApronStore, RecencyStore, given}
import sturdy.values.{Finite, Join, MaybeChanged, Widen}
import sturdy.values.references.{*, given}

import scala.reflect.ClassTag

object ApronRecencyStore:
  def apply[Ctx: Ordering: Finite, Type: ApronType: Join: Widen](apronManager: Manager):
      (RecencyStore[Ctx, PowVirtualAddress[Ctx], ApronExpr[PhysicalAddress[Ctx], Type]], ApronStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], ApronExpr[PhysicalAddress[Ctx], Type]]) =
    type VirtAddr = VirtualAddress[Ctx]
    type PhysAddr = PhysicalAddress[Ctx]
    type PowPhysAddr = PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]]
    type PowVirtAddr = PowVirtualAddress[Ctx]
    type ApronExprVirtAddr = ApronExpr[VirtualAddress[Ctx], Type]
    type ApronExprPhysAddr = ApronExpr[PhysicalAddress[Ctx], Type]

    // Special case join on apron expression used only in ApronStore.
    // We can assume that we only join on constant apron expressions.
    given Join[ApronExprPhysAddr] = {
      case (ApronExpr.Constant(coeff1,tpe1), ApronExpr.Constant(coeff2, tpe2)) =>
        val joinedCoeff = Join(coeff1, coeff2)
        val joinedTypes = Join(tpe1, tpe2)
        MaybeChanged(ApronExpr.Constant(joinedCoeff.get, joinedTypes.get), joinedCoeff.hasChanged || joinedTypes.hasChanged)
      case (e1,e2) => throw IllegalArgumentException(s"Unexpected apron expression as arguments to join ${(e1,e2)}")
    }
  
    val apronStore: ApronStore[Ctx, Type, PowPhysAddr, ApronExprPhysAddr] = ApronStore(
      apronManager,
      Abstract1(apronManager, new Environment()),
      Map(),
      getIntVal = Some[ApronExprPhysAddr](_),
      makeIntVal = (expr: ApronExprPhysAddr, state: Abstract1) =>
        ApronExpr.Constant(state.getBound(apronManager, expr.toIntern(state.getEnvironment)), expr._type)
    )

    val addressTranslation: AddressTranslation[Ctx] = AddressTranslation.empty

    val recencyStore: RecencyStore[Ctx, PowVirtAddr, ApronExprPhysAddr] =
      RecencyStore(
        apronStore,
        addressTranslation)

    (recencyStore, apronStore)
