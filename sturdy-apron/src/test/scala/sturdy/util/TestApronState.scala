package sturdy.util

import apron.*
import sturdy.apron.{ApronExpr, ApronRecencyState, ApronState}
import sturdy.effect.{EffectList, EffectStack}
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, WithWideningThresholds}
import sturdy.util.TestContexts.*
import sturdy.util.TestTypes.*
import sturdy.values.Finite
import sturdy.values.references.VirtualAddress

type VirtAddr = VirtualAddress[Ctx]

def withApronState[T](using apronManager: Manager)(f: (Manager, CollectedFailures[FailureKind], EffectStack, ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]]) ?=> T): T =
  given Finite[FailureKind] with {}
  given failure: CollectedFailures[FailureKind] = new CollectedFailures[FailureKind]()
  var apronState: ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]] = null
  given effectStack: EffectStack = new EffectStack(RecencyClosure(apronState.recencyStore, failure))
  given WithWideningThresholds = WithWideningThresholds.No
  apronState = RecencyRelationalStore[Ctx, Type]
  given ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]] = apronState
  f
