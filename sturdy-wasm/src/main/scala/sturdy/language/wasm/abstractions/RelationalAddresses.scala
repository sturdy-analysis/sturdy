package sturdy.language.wasm.abstractions

import sturdy.language.wasm.generic.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.fix.DomLogger
import sturdy.values.Finite
import sturdy.values.references.{*, given}

trait RelationalAddresses extends RelationalTypes:
  enum RelAddr:
    case Local(callFrameAddr: Int, frame: FrameData)
    case Temp(fixin: FixIn, tpe: Type)

  final type VirtAddr = VirtualAddress[RelAddr]
  final type PhysAddr = PhysicalAddress[RelAddr]
  final type PowVirtAddr = PowVirtualAddress[RelAddr]
  final type PowPhysAddr = PowersetAddr[PhysAddr, PhysAddr]

  def tempRelationalAlloc(rootFrameData: FrameData)(using domLogger: DomLogger[FixIn]): AAllocatorFromContext[Type, RelAddr] = AAllocatorFromContext(
    tpe => RelAddr.Temp(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module)), tpe)
  )
  given localRelationaAlloc: AAllocatorFromContext[(Int, FrameData, Option[InstLoc]), RelAddr] = AAllocatorFromContext(
    (i, data, _) => RelAddr.Local(i, data)
  )

  given Ordering[RelAddr] = Ordering.by {
    case RelAddr.Local(callFrameAddr, data) => Left(callFrameAddr, data)
    case RelAddr.Temp(fixin, tpe) => Right((fixin, tpe))
  }
  given Finite[RelAddr] with {}
  
  given Ordering[VirtAddr] = VirtualAddressOrdering


