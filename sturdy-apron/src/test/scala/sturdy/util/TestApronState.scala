package sturdy.util

import apron.*
import sturdy.apron.{ApronExpr, ApronRecencyState, ApronState}
import sturdy.effect.{EffectList, EffectStack}
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore}
import sturdy.util.TestContexts.*
import sturdy.util.TestTypes.*
import sturdy.values.Finite
import sturdy.values.references.VirtualAddress

type VirtAddr = VirtualAddress[Ctx]

def withApronState[T](using apronManager: Manager)(f: (Manager, CollectedFailures[FailureKind], EffectStack, ApronState[VirtAddr,Type]) ?=> T): T =
  given Finite[FailureKind] with {}
  given failure: CollectedFailures[FailureKind] = new CollectedFailures[FailureKind]()
  var apronState: ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]] = null
  given effectStack: EffectStack = new EffectStack(EffectList(
    RecencyClosure(apronState.recencyStore), failure
  ))
  apronState = RecencyRelationalStore[Ctx, Type]
  given ApronState[VirtAddr, Type] = apronState
  f
